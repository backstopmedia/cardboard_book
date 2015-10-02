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
package net.dystopiazero.theglitcher.notifications;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import net.dystopiazero.theglitcher.Glitcher_Utils;
import net.dystopiazero.theglitcher.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

public class Glitcher_BitmapNotification implements Glitcher_Notification
{
	private Bitmap bitmap;
	private int[] bmap_data;
	private IntBuffer buff;
	private Context context;
	private boolean isReady=false;
	
	private FloatBuffer fb;
	private FloatBuffer tex_coords;
	
	private int v_shade_id, f_shade_id, s_program_id;
	
	private float[] screen_rect=
		   {-0.7f,0.1f,0.0f,
			-0.7f,-0.1f,0.0f,
			0.7f,-0.1f,0.0f,
			0.7f,-0.1f,0.0f,
			0.7f,0.1f,0.0f,
			-0.7f,0.1f,0.0f};
	
	private float[] tex_rect=
		{ 0.0f, 0.0f,
		  0.0f, 1.0f,
		  1.0f, 1.0f,
		  1.0f, 1.0f,
		  1.0f, 0.0f,
		  0.0f, 0.0f };
	
	private String v_shader=
			"attribute vec4 gFrame;\n"+
			"attribute vec2 tex_pos;\n"+
			"varying vec2 gTexCoord;\n"+
								
			"void main()\n"+
			"{\n"+
				"gl_Position=gFrame;\n"+
				"gTexCoord=tex_pos;\n"+
			"}";
	
	private String f_shader=
			"precision mediump float;\n"+
	
			"varying vec2 gTexCoord;\n"+
			"uniform sampler2D gNotifTex;\n"+
			"uniform float gTime;\n"+
								
			"void main()\n"+
			"{\n"+
				"vec4 color=texture2D(gNotifTex,gTexCoord);\n"+
				
				"gl_FragColor=vec4(color.rgb,0.5);\n"+
			"}";
	
	
	private int[] texID=new int[1];
	private double start_time;
	private double end_time;
	private int resource_id;
	private double ttl;
	
	private boolean isResource;
		
	public Glitcher_BitmapNotification(Context context, int id, double ttl)
	{
		isReady=false;
		
		this.context=context;
		this.resource_id=id;
		this.ttl=ttl;
		this.isResource=true;
		
		start_time=System.nanoTime()/1000000.0;
		end_time=start_time+ttl;
	}
	
	public Glitcher_BitmapNotification(Context context, Bitmap bitmap, double ttl)
	{
		isReady=false;
		
		this.bitmap=bitmap;
		this.context=context;
		this.resource_id=0;
		this.ttl=ttl;
		this.isResource=false;
		
		start_time=System.nanoTime()/1000000.0;
		end_time=start_time+ttl;
	}
	
	public Glitcher_BitmapNotification(Context context, int id, double ttl, float[] rect)
	{
		isReady=false;
		
		this.context=context;
		this.resource_id=id;
		this.ttl=ttl;
		this.isResource=true;
		
		if (rect.length>=4)
		{
			screen_rect=new float[]
			{
				rect[0],rect[1],0.0f,
				rect[0],rect[3],0.0f,
				rect[2],rect[3],0.0f,
				rect[2],rect[3],0.0f,
				rect[2],rect[1],0.0f,
				rect[0],rect[1],0.0f
			};
		}
		
		start_time=System.nanoTime()/1000000.0;
		end_time=start_time+ttl;
	}
	
	public Glitcher_BitmapNotification(Context context, Bitmap bitmap, double ttl, float[] rect)
	{
		isReady=false;
		
		this.bitmap=bitmap;
		this.context=context;
		this.resource_id=0;
		this.ttl=ttl;
		this.isResource=false;
		
		if (rect.length>=4)
		{
			screen_rect=new float[]
			{
				rect[0],rect[1],0.0f,
				rect[0],rect[3],0.0f,
				rect[2],rect[3],0.0f,
				rect[2],rect[3],0.0f,
				rect[2],rect[1],0.0f,
				rect[0],rect[1],0.0f
			};
		}
		
		start_time=System.nanoTime()/1000000.0;
		end_time=start_time+ttl;
	}
	
	private void build()
	{
		loadBitmap();
		setupRect();
		compileShaders();
		
		GLES20.glGenTextures(1,texID,0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texID[0]);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buff);
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
	
	private void compileShaders()
	{
		v_shade_id=0;
		f_shade_id=0;
		s_program_id=0;
		
		int[] ids=new int[3];
		isReady=Glitcher_Utils.compileShaders(v_shader,f_shader,ids);
		
		v_shade_id=ids[Glitcher_Utils.V_SHADE];
		f_shade_id=ids[Glitcher_Utils.F_SHADE];
		s_program_id=ids[Glitcher_Utils.S_PROG];
	}
	
	private void loadBitmap()
	{
		if (isResource)
			bitmap=BitmapFactory.decodeResource(context.getResources(), resource_id);
		
		bmap_data=new int[bitmap.getWidth()*bitmap.getHeight()];
		bitmap.getPixels(bmap_data, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
		buff=IntBuffer.wrap(bmap_data);
	}
	
	@Override
	public double getTTL() 
	{
		return ttl;
	}

	@Override
	public void draw(double time) 
	{
		if (!isReady())
			build();
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		//GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		
		GLES20.glUseProgram(this.s_program_id);
		
		
		int texloc=GLES20.glGetUniformLocation(s_program_id,"gNotifTex");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texID[0]);
		GLES20.glUniform1i(texloc,2);
		
		int tcoord=GLES20.glGetAttribLocation(s_program_id, "tex_pos");
		GLES20.glEnableVertexAttribArray(tcoord);
		GLES20.glVertexAttribPointer(tcoord, 2, GLES20.GL_FLOAT, true, 0, tex_coords);
		
		int pos=GLES20.glGetAttribLocation(s_program_id, "gFrame");
		GLES20.glEnableVertexAttribArray(pos);
		GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 0, fb);
		
		int timeLoc=GLES20.glGetUniformLocation(s_program_id, "gTime");
		GLES20.glUniform1f(timeLoc,(float)time);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		GLES20.glDisableVertexAttribArray(pos);
		GLES20.glDisableVertexAttribArray(tcoord);
		
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
	}

	@Override
	public void cleanup()
	{
		GLES20.glDeleteTextures(1,texID,0);
		
		GLES20.glDeleteProgram(s_program_id);
		GLES20.glDeleteShader(v_shade_id);
		GLES20.glDeleteShader(f_shade_id);
	}

	@Override
	public double getStartTime() 
	{
		return start_time;
	}

	@Override
	public double getEndTime() 
	{
		return end_time;
	}
	
	public boolean isReady()
	{
		return isReady;
	}
}
