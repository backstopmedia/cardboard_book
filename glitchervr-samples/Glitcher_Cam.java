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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.List;

import net.dystopiazero.theglitcher.notifications.Glitcher_BitmapNotification;
import net.dystopiazero.theglitcher.notifications.Glitcher_NotificationScheduler;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

public class Glitcher_Cam implements SurfaceTexture.OnFrameAvailableListener, Camera.PictureCallback, Camera.AutoFocusCallback
{
	private Context context;
	
	private Camera camera;
	private Camera.Parameters cam_params;
	
	private MediaRecorder rec;
	private boolean isRecording;
	
	private final int WIDTH=0;
	private final int HEIGHT=1;
	
	private boolean isActive;
	private int cam_id;
	
	private int[] dimensions=new int[2];
	
	private Glitcher_CamSurface surface=null;
	private SurfaceTexture tex=null;
	
	private String vidPath="";
	
	public Glitcher_Cam(Glitcher_CamSurface surf, int id, Context context)
	{
		this.context=context;
		
		camera=null;
		cam_params=null;
		cam_id=id;
		
		isRecording=false;
		isActive=false;
		
		dimensions[0]=0;
		dimensions[1]=0;
		
		surface=surf;
		tex=new SurfaceTexture(surf.getTextureID());
		tex.setOnFrameAvailableListener(this);
		surface.setSurfaceTexture(tex);
	}
	
	public Glitcher_CamSurface getSurface()
	{
		return surface;
	}
	
	public int getID()
	{
		return cam_id;
	}
	
	public boolean start()
	{
		if (camera!=null || surface==null)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,start: Cannot start cam("+cam_id+"), a camera is already active!");
			return false;
		}
		
		try
		{
			camera=Camera.open(cam_id);
		}
		
		catch(Exception e)
		{
			isActive=false;
			
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,start: Error, cannot start cam("+cam_id+"): "+e.getMessage());
			
			return false;
		}
		
		if (camera==null)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,start: Error, cam("+cam_id+") is null.");
			return false;
		}
		
		cam_params=camera.getParameters();
		adjustCameraParams(cam_params);
		camera.setParameters(cam_params);
		
		Camera.Size size=cam_params.getPreviewSize();
		dimensions[WIDTH]=size.width;
		dimensions[HEIGHT]=size.height;
		
		surface.resetDimensions(size.width,size.height);
		
		try
		{
			camera.setPreviewTexture(tex);
		}
		
		catch(Exception e)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,start: Could not set preview texture for camera.");
			return false;
		}
		
		camera.startPreview();
		
		camera.setFaceDetectionListener(surface);
		
		try
		{
			camera.startFaceDetection();
		}
		
		catch(IllegalArgumentException e)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,start: Could not start face detection - "+e.getMessage());
		}
		
		isActive=true;
		
		return true;
	}
	
	private void adjustCameraParams(Camera.Parameters params)
	{
		List<Camera.Size> sizes=params.getSupportedPictureSizes();
		
		int max_width=-1,max_height=-1;
		for(Iterator<Camera.Size> i=sizes.iterator(); i.hasNext();)
		{
			Camera.Size size=i.next();
			if (size.width>max_width)
			{	
				max_width=size.width;
				max_height=size.height;
			}
		}
		
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,adjustCameraParams: Setting photo dimensions to ("+max_width+","+max_height+")");
		params.setPictureSize(max_width,max_height);
		
		
		sizes=params.getSupportedPreviewSizes();
		max_width=-1;
		max_height=-1;
		for(Iterator<Camera.Size> i=sizes.iterator(); i.hasNext();)
		{
			Camera.Size size=i.next();
			if (size.width>max_width)
			{	
				max_width=size.width;
				max_height=size.height;
			}
		}
		
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,adjustCameraParams: Setting preview dimensions to ("+max_width+","+max_height+")");
		params.setPreviewSize(max_width,max_height);
		
		//params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		//params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
		
		params.setJpegQuality(100);
		params.setVideoStabilization(true);
	}
	
	public boolean stop()
	{
		if (isRecording)
			stopVideoRecord();
		
		if (camera!=null)
		{
			try
			{
				stopFaceDetection();
				camera.stopPreview();
				camera.release();
				camera=null;
			}
			
			catch(RuntimeException e)
			{
				Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,stop: Could not stop camera - "+e.getMessage());
			}
		}
		
		isActive=false;
		isRecording=false;
		
		return true;
	}
	
	public boolean pause()
	{
		return true;
	}
	
	public boolean resume()
	{
		return true;
	}
	
	public boolean isActive()
	{
		return isActive;
	}
	
	public boolean isRecording()
	{
		return isRecording;
	}
	
	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) 
	{
		surface.requestRender();
	}
	
	public void enableTorch()
	{
		Camera.Parameters params=camera.getParameters();
		params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		camera.setParameters(params);
	}
	
	public void disableTorch()
	{
		Camera.Parameters params=camera.getParameters();
		params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		camera.setParameters(params);
	}
	
	public void takePicture()
	{
		camera.takePicture(null, null, null, this);
	}
	
	public int getPreviewWidth()
	{
		return dimensions[WIDTH];
	}
	
	public int getPreviewHeight()
	{
		return dimensions[HEIGHT];
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) 
	{
		final String mPath = Environment.getExternalStorageDirectory().toString()+"/"+(System.currentTimeMillis()/3600)+".jpg";
		Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length);
		
		try
		{
			File file=new File(mPath);
			FileOutputStream fout=new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
			fout.flush();
			fout.close();
			
			Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,onPictureTaken: Saved photo to "+mPath);
		}
		
		catch(Exception e)
		{
			Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,onPictureTaken: Could not save photo - "+e.getMessage());
		}
		
		MediaScannerConnection.scanFile(context,new String[]{mPath},new String[]{"jpg"},null);
		
		camera.startPreview();
		
		try
		{
			camera.startFaceDetection();
		}
		
		catch(IllegalArgumentException e)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,onPictureTaken: Could not start face detection - "+e.getMessage());
		}
	}
	
	public void focus()
	{
		camera.autoFocus(this);
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) 
	{
		Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
		Glitcher_BitmapNotification bn;
		
		if (success)
			bn=new Glitcher_BitmapNotification(surface.getActivityContext(),R.drawable.afsuccess, 2000.0);
		else
			bn=new Glitcher_BitmapNotification(surface.getActivityContext(),R.drawable.affailed, 2000.0);
		
		ns.add(bn);
		
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,onAutoFocus: "+success);
	}

	public void startFaceDetection()
	{
		camera.startFaceDetection();
	}
	
	public void stopFaceDetection()
	{
		try
		{
			camera.stopFaceDetection();
		}
		
		catch(Exception e)
		{
		}
	}
	
	public void startVideoRecord()
	{
		if (isRecording)
			return;
		
		rec=new MediaRecorder();
		
		rec.setCamera(camera);
		rec.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		rec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		
		rec.setProfile(CamcorderProfile.get(cam_id, CamcorderProfile.QUALITY_HIGH));
		
		vidPath = Environment.getExternalStorageDirectory().toString()+"/"+(System.currentTimeMillis()/3600)+".mp4";
		
		rec.setOutputFile(vidPath);
		
		camera.stopFaceDetection();
		//camera.stopPreview();
		camera.unlock();
		
		//rec.setPreviewDisplay(dummy.getHolder().getSurface());
		
		try
		{
			rec.prepare();
		}
		
		catch(IOException e)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,startVideoRecord: Could not start recording - "+e.getMessage());
			return;
		}
		
		rec.start();
		
		isRecording=true;
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,startVideoRecord: started.");
	}
	
	public void stopVideoRecord()
	{
		if (isRecording && rec!=null)
		{
			rec.stop();
			rec.reset();
			rec.release();
			
			rec=null;
			
			camera.lock();
			
			stop();
			start();
			
			MediaScannerConnection.scanFile(context,new String[]{vidPath},new String[]{"mp4"},null);
			
			isRecording=false;
			Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Cam,stopVideoRecord: stopped.");
		}
	}
}
