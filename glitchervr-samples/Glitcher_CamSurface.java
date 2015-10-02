/*The MIT License (MIT)

Copyright (c) 2015 Mike Pasamonik

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/
package net.dystopiazero.theglitcher;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.EyeParams;
import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import net.dystopiazero.theglitcher.effects.Glitcher_Effect;
import net.dystopiazero.theglitcher.effects.Glitcher_EffectManager;
import net.dystopiazero.theglitcher.menu.Glitcher_MenuManager;
import net.dystopiazero.theglitcher.notifications.Glitcher_BitmapNotification;
import net.dystopiazero.theglitcher.notifications.Glitcher_Notification;
import net.dystopiazero.theglitcher.notifications.Glitcher_NotificationScheduler;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

public class Glitcher_CamSurface extends CardboardView implements CardboardView.StereoRenderer, SurfaceHolder.Callback, Glitcher_Surface, Camera.FaceDetectionListener
{
	private Vector<Glitcher_SurfaceListener> listeners;
	
	private boolean surface_created=false;
	private boolean initialized=false;
	private boolean shot_activate=false;
	
	private boolean vr_mode;
	
	private Context context;
	
	private float[] screen_rect=
		   {-1.0f,1.0f,1.0f,
			-1.0f,-1.0f,1.0f,
			1.0f,-1.0f,1.0f,
			1.0f,-1.0f,1.0f,
			1.0f,1.0f,1.0f,
			-1.0f,1.0f,1.0f};
	
	private float[] tex_rect=
			{ 0.0f, 0.0f,
			  0.0f, 1.0f,
			  1.0f, 1.0f,
			  1.0f, 1.0f,
			  1.0f, 0.0f,
			  0.0f, 0.0f };
	
	private float[] mModel;
	private float[] mCam;
	private float[] mView;
	private float[] mModelView;
	private float[] mMVP;
	
	private int n_faces;
	private boolean faces_detected=false;
	private float faces[][];
	
	private final int NOISE_WIDTH=160;
	private final int NOISE_HEIGHT=160;
	private int[] noiseID;
	private IntBuffer noiseMap;
	
	private FloatBuffer fb;
	private FloatBuffer tex_coords;
	
	private int[] tex_id;
	private SurfaceTexture tex;
	
	private int width,height;
	private final long startTime=System.currentTimeMillis();
	private Random random=new Random(System.nanoTime());
	
	private Glitcher_NotificationScheduler nf;
	
	public Glitcher_CamSurface(Context context) 
	{
		super(context);
		
		this.context=context;
		
		width=0;
		height=0;
		
		vr_mode=true;
		
		mModel=new float[16];
		mCam=new float[16];
		mView=new float[16];
		mModelView=new float[16];
		mMVP=new float[16];
		
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);
		setEGLConfigChooser(8,8,8,8,16,0);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		getHolder().addCallback(this);
		
		listeners=new Vector<Glitcher_SurfaceListener>();
		nf=Glitcher_NotificationScheduler.getInstance();		
		
		setRenderer(this);
	}
	
	public Glitcher_CamSurface(Context context, AttributeSet as)
	{
		super(context,as);
		
		this.context=context;
		
		width=0;
		height=0;
		
		vr_mode=true;
		
		mModel=new float[16];
		mCam=new float[16];
		mView=new float[16];
		mModelView=new float[16];
		mMVP=new float[16];
		
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);
		setEGLConfigChooser(8,8,8,8,16,0);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		getHolder().addCallback(this);
		
		listeners=new Vector<Glitcher_SurfaceListener>();
		nf=Glitcher_NotificationScheduler.getInstance();
		
		setRenderer(this);
	}
	
	public void init(int width, int height)
	{
		if (!surface_created)
			return;
		
		Matrix.setIdentityM(mModel,0);
		Matrix.scaleM(mModel,0,10.0f,10.0f,10.0f);
		Matrix.translateM(mModel,0,0,0,-2.0f);
		Matrix.setLookAtM(mCam,0,0,0,3.0f,0,0,0,0,1.0f,0);
		
		Glitcher_HeadTracker tracker=Glitcher_HeadTracker.getInstance();
		tracker.setCam(mCam);
		
		resetDimensions(width,height);
		
		setupRect();
		noiseID=new int[1];
		GLES20.glGenTextures(1,noiseID,0);
		genNoise(NOISE_WIDTH,NOISE_HEIGHT);
		
		initialized=true;
		
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamSurface,init: Initialized");
		//Log.d(Glitcher_Constants.LOG_TAG,"Extensions: "+GLES20.glGetString(GLES20.GL_EXTENSIONS));
		
		for(Iterator<Glitcher_SurfaceListener> i=listeners.iterator(); i.hasNext();)
		{
			Glitcher_SurfaceListener l=i.next();
			l.onSurfaceInitialized(this.getHolder());
		}
	}
	
	
	private void setupRect()
	{
		ByteBuffer bb=ByteBuffer.allocateDirect(12*8);
		bb.order(ByteOrder.nativeOrder());
		
		fb=bb.asFloatBuffer();
		fb.put(screen_rect);
		fb.position(0);
		
		ByteBuffer bc=ByteBuffer.allocateDirect(8*8);
		bc.order(ByteOrder.nativeOrder());
		tex_coords=bc.asFloatBuffer();
		tex_coords.put(tex_rect);
		tex_coords.position(0);
	}
	
	//@Override
	public void onDrawFrame(GL10 gl) 
	{
		if (!surface_created || !initialized || tex==null)
			return;
		
		float time=(System.currentTimeMillis()-startTime)/1000.0f;
		
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		renderCameraOutput(time);
		renderNotifications();
		
		Glitcher_MenuManager man=Glitcher_MenuManager.getInstance();
		man.draw();
	}
	
	
	private void renderNotifications()
	{
		nf.clearDeadNotifications();
		nf.run();
	}
	
	private void renderCameraOutput(float time)
	{
		Glitcher_EffectManager em=Glitcher_EffectManager.getInstance();
		Glitcher_Effect e=em.getActiveEffect();
		
		if (e==null)
			return;
		
		Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
		Glitcher_HeadTracker tracker=Glitcher_HeadTracker.getInstance();
		
		if (cam.getActiveCam()!=null)
			tex.updateTexImage();
		
		GLES20.glUseProgram(e.getEffectID());
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		Matrix.multiplyMM(mModelView,0,mCam,0,mModel,0);
		Matrix.multiplyMM(mMVP,0,tracker.getEyePerspect(),0,mModelView,0);
		
		e.prerender(this);
		
		int mvploc=GLES20.glGetUniformLocation(e.getEffectID(), "mvp");
		GLES20.glUniformMatrix4fv(mvploc, 1, false, mMVP, 0);
		
		int mvloc=GLES20.glGetUniformLocation(e.getEffectID(), "mv");
		GLES20.glUniformMatrix4fv(mvloc, 1, false, mModelView, 0);
		
		int camtexloc=GLES20.glGetUniformLocation(e.getEffectID(),"gTexture");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex_id[0]);
		GLES20.glUniform1i(camtexloc,0);
		
		int noisetexloc=GLES20.glGetUniformLocation(e.getEffectID(),"gNoise");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, noiseID[0]);
		GLES20.glUniform1i(noisetexloc,1);
		
		int tcoord=GLES20.glGetAttribLocation(e.getEffectID(), "tex_coord");
		GLES20.glEnableVertexAttribArray(tcoord);
		GLES20.glVertexAttribPointer(tcoord, 2, GLES20.GL_FLOAT, true, 0, tex_coords);
		
		int pos=GLES20.glGetAttribLocation(e.getEffectID(), "gPosition");
		GLES20.glEnableVertexAttribArray(pos);
		GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 0, fb);
		
		int timeLoc=GLES20.glGetUniformLocation(e.getEffectID(), "gTime");
		GLES20.glUniform1f(timeLoc,time);
		
		int res=GLES20.glGetUniformLocation(e.getEffectID(), "gResolution");
		GLES20.glUniform2f(res, width, height);
		
		if (!faces_detected)
			n_faces=0;
		
		int num_faces=GLES20.glGetUniformLocation(e.getEffectID(), "gNumFaces");
		GLES20.glUniform1i(num_faces, n_faces);
		
		if (faces_detected)
		{
			for(int k=0; k<n_faces; k++)
			{
				//FloatBuffer facebuff=FloatBuffer.wrap((faces[k]));
				int face_loc=GLES20.glGetUniformLocation(e.getEffectID(), "gFace"+k);
				GLES20.glUniform4f(face_loc, faces[k][0],faces[k][1],faces[k][2],faces[k][3]);
			}
		}
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		GLES20.glDisableVertexAttribArray(pos);
		GLES20.glDisableVertexAttribArray(tcoord);
		
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		
		e.renderAdditionalAssets();
		
		faces_detected=false;
	}

	@Override
	public void onSurfaceChanged(int width, int height) 
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamSurface,onSurfaceChanged: ("+width+","+height+")");
		resetDimensions(width,height);
	
	    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
	    GLES20.glEnable(GLES20.GL_BLEND);
	    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA); 

	    setVRModeEnabled(vr_mode);
	   
	    //GLES20.glViewport(0, 0, width,  height);
	    //GLES20.glEnable(GLES20.GL_TEXTURE_2D);
	    
	    for(Iterator<Glitcher_SurfaceListener> i=listeners.iterator(); i.hasNext();)
		{
			Glitcher_SurfaceListener l=i.next();
			l.onSurfaceChanged(this.getHolder());
		}
	    
	    init(width,height);
	}

	@Override
	public void onSurfaceCreated(EGLConfig config) 
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamSurface,onSurfaceCreated");
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.2f);
		surface_created=true;
		
		for(Iterator<Glitcher_SurfaceListener> i=listeners.iterator(); i.hasNext();)
		{
			Glitcher_SurfaceListener l=i.next();
			l.onSurfaceCreated(this.getHolder());
		}
	}
	
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		super.surfaceDestroyed(holder);
		
		for(Iterator<Glitcher_SurfaceListener> i=listeners.iterator(); i.hasNext();)
		{
			Glitcher_SurfaceListener l=i.next();
			l.onSurfaceHolderDestroyed(holder);
		}
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		super.surfaceCreated(holder);
		
		for(Iterator<Glitcher_SurfaceListener> i=listeners.iterator(); i.hasNext();)
		{
			Glitcher_SurfaceListener l=i.next();
			l.onSurfaceHolderCreated(holder);
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
		super.surfaceChanged(holder,format,width,height);
		
		for(Iterator<Glitcher_SurfaceListener> i=listeners.iterator(); i.hasNext();)
		{
			Glitcher_SurfaceListener l=i.next();
			l.onSurfaceHolderChanged(holder,width,height);
		}
	}
	
	public boolean isCreated()
	{
		return surface_created;
	}
	
	public void setSurfaceTexture(SurfaceTexture _tex)
	{
		tex=_tex;
	}
	
	public int createNewTextureID()
	{
		tex_id=new int[1];
		GLES20.glGenTextures(1, tex_id, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,tex_id[0]);
		
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);        
	    GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
	    
		return tex_id[0];
	}
	
	public int getTextureID()
	{
		return (tex_id!=null)?tex_id[0]:createNewTextureID();
	}
	
	public void resetDimensions(int width, int height)
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamSurface,resetDimensions: ("+width+","+height+")");
		
		this.width=width;
		this.height=height;
	}

	@Override
	public void addSurfaceListener(Glitcher_SurfaceListener listener) 
	{
		if (listener!=null)
			listeners.add(listener);
	}

	@Override
	public void onDrawEye(EyeTransform arg0) 
	{
		Glitcher_HeadTracker tracker=Glitcher_HeadTracker.getInstance();
		tracker.update(arg0);
		
		onDrawFrame(null);
	}

	@Override
	public void onFinishFrame(Viewport arg0) 
	{
		if (shot_activate)
		{
			readPixelData();
			shot_activate=false;
		}
	}

	@Override
	public void onNewFrame(HeadTransform headTransform) 
	{
		Glitcher_HeadTracker tracker=Glitcher_HeadTracker.getInstance();
		tracker.update(headTransform);
	}

	@Override
	public void onRendererShutdown() 
	{
	}
	
	void genNoise(int width, int height)
	{
		int[] noise=new int[width*height];
		ByteBuffer bb=ByteBuffer.allocateDirect(width*height*4);
		bb.order(ByteOrder.nativeOrder());
		noiseMap=bb.asIntBuffer();
		
		for(int k=0; k<height; k++)
		{
			for(int j=0; j<width; j++)
			{
				int r=random.nextInt(255);
				int g=random.nextInt(255);
				int b=random.nextInt(255);
				
				int col=((r<<16)&0x00ff0000)|((g<<8)&0x0000ff00)|((b<<0)&0x000000ff)|0xff000000;
				
				noise[j+(width*k)]=col;
			}
		}
		
		noiseMap.put(noise);
		noiseMap.position(0);
		
		if (noiseID[0]!=0)
		{
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,noiseID[0]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );
	        
	        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, noiseMap);
		}
	}
	
	public void takeScreenshot()
	{
		shot_activate=true;
	}
	
	private void readPixelData()
	{
		Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
		final String mPath = Environment.getExternalStorageDirectory().toString()+"/"+(System.currentTimeMillis()/3600)+".jpg";
		
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamSurface,readPixelData: Saving screenshot to "+mPath);
		
		if (cam.getActiveCam()==null)
			return;
		
		int w=cam.getActiveCam().getPreviewWidth();
		int h=cam.getActiveCam().getPreviewHeight();
		
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamSurface,readPixelData: Screenshot dimensions ("+w+","+h+")");
		
		/*** SGS3 FIX FOR GL_OUT_OF_MEMORY BELOW - STILL GENERATED EVEN WHEN READING SCANLINES ***/
		/*ByteBuffer b=ByteBuffer.allocateDirect(w*4);
		b.order(ByteOrder.nativeOrder());
		
		int[] processed=new int[w*h];
		GLES20.glFlush();
		
		for(int k=0; k<h; k++)
		{
			b.position(0);
			GLES20.glReadPixels(0,k,w,1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, b);
					
			byte[] data=b.array();
			
			for(int j=0; j<w; j++)
			{
				int index=j*4;
			
				int scanline=(((h-1)*w)-(k*w))+j;
			
				processed[scanline]=((data[index+3]<<24)&0xff000000)|
				((data[index]<<16)&0x00ff0000)|
				((data[index+1]<<8)&0x0000ff00)|
				((data[index+2])&0x000000ff);
			}
        }*/
		
		
		ByteBuffer b=ByteBuffer.allocateDirect(w*h*4);
		b.order(ByteOrder.nativeOrder());
		b.position(0);
		
		GLES20.glFlush();
		GLES20.glReadPixels(0,0,w,h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, b);
					
		byte[] data=b.array();
		int[] processed=new int[w*h];
		
		for(int k=0; k<w*h; k++)
		{
			int index=k*4;
			
			int scanline=((h-(int)(k/w)-1)*w)+(k%w);
			
			processed[scanline]=((data[index+3]<<24)&0xff000000)|
					((data[index]<<16)&0x00ff0000)|
					((data[index+1]<<8)&0x0000ff00)|
					((data[index+2])&0x000000ff);
        }
		
		Bitmap bitmap=Bitmap.createBitmap(processed,w,h,Bitmap.Config.ARGB_8888);
		
		try
		{
			File file=new File(mPath);
			FileOutputStream fout=new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
			fout.flush();
			fout.close(); 
		}
		
		catch(Exception e)
		{
			Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamSurface,readPixelData: could not take screenshot - "+e.getMessage());
		}
		
		MediaScannerConnection.scanFile(context,new String[]{mPath},new String[]{"jpg"},null);
		
		Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
		Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(context,R.drawable.framecaptured, 5000.0);
		ns.add(bn);
	}
	
	public Context getActivityContext()
	{
		return this.context;
	}

	@Override
	public void onFaceDetection(Face[] faces, Camera camera) 
	{
		Glitcher_CamManager cm=Glitcher_CamManager.getInstance();
		Glitcher_Cam cam=cm.getActiveCam();
		
		if (cam==null)
			return;
		
		int w=cam.getPreviewWidth();
		int h=cam.getPreviewHeight();
		
		n_faces=faces.length;
		
		this.faces=new float[n_faces][4];
		for(int k=0; k<faces.length; k++)
		{
			float _x=((faces[k].rect.left+1000.0f)/2000.0f);
			float _y=((faces[k].rect.top+1000.0f)/2000.0f);
			float _w=((faces[k].rect.right+1000.0f)/2000.0f);
			float _h=((faces[k].rect.bottom+1000.0f)/2000.0f);
			
			this.faces[k][0]=_x;
			this.faces[k][1]=_y;
			this.faces[k][2]=_w;
			this.faces[k][3]=_h;
			
			//Log.d(Glitcher_Utils.LOG_TAG,"Found face "+k+" ("+this.faces[k][0]+","+this.faces[k][1]+","+this.faces[k][2]+","+this.faces[k][3]+")");
		}
		
		faces_detected=true;
	}
	
	public int getNumFaces()
	{
		return n_faces;
	}
	
	public float[][] getFaces()
	{
		return faces;
	}
	
	public boolean getFacesDetected()
	{
		return faces_detected;
	}
	
	public void toggleVRMode()
	{
		vr_mode=!vr_mode;
		setVRModeEnabled(vr_mode);
	}
	
	public boolean isVRMode()
	{
		return vr_mode;
	}
	
	public int getViewportWidth()
	{
		return width;
	}
	
	public int getViewportHeight()
	{
		return height;
	}
}
