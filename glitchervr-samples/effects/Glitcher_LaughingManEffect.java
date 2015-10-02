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
package net.dystopiazero.theglitcher.effects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;



import net.dystopiazero.theglitcher.Glitcher_CamSurface;
import net.dystopiazero.theglitcher.Glitcher_Utils;
import net.dystopiazero.theglitcher.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

public class Glitcher_LaughingManEffect extends Glitcher_BasicEffect implements Glitcher_Effect 
{
	
	private String f_shader=
			"precision mediump float;\n"+
			"uniform sampler2D gLaughing;\n"+
			"varying vec2 gTexCoord;\n"+
									
			"void main()\n"+
			"{\n"+
				"vec4 color=texture2D(gLaughing,gTexCoord.xy);\n"+
				
				"if (color.r<=0.2 && color.g<=0.2 && color.b<=0.2)\n"+
					"discard;\n"+
				
				"gl_FragColor=color;\n"+
			"}";
		
	private float[] tex_rect=
		{ 0.0f, 0.0f,
		  0.0f, 1.0f,
		  1.0f, 1.0f,
		  1.0f, 1.0f,
		  1.0f, 0.0f,
		  0.0f, 0.0f };
	
	private FloatBuffer fb;
	private FloatBuffer tex_coords;
	
	private int n_faces=0;
	private float[][] faces;
	
	private int texid[]=new int[1];
	private Bitmap bitmap;
	private IntBuffer buff;
	private int[] bitmap_data;
	
	private int[] shader_ids=new int[3];

	private Context context;
	private boolean isReady=false;
		
	protected String effectName="Laughing Man Effect";
			
	public Glitcher_LaughingManEffect()
	{	
	}
	
	@Override
	public void prerender(Glitcher_CamSurface surf)
	{
		n_faces=surf.getNumFaces();
		faces=surf.getFaces();
		context=surf.getActivityContext();
		
		if (!isReady)
		{
			buildTexture();
		}
		
		int glaughloc=GLES20.glGetUniformLocation(this.getEffectID(), "gLaughing");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texid[0]);
		GLES20.glUniform1i(glaughloc,3);
	}
		
	@Override
	public boolean buildEffect()
	{
		super.buildEffect();
		
		return true;
	}
		
	private void buildTexture()
	{
		ByteBuffer bc=ByteBuffer.allocateDirect(8*8);
		bc.order(ByteOrder.nativeOrder());
		tex_coords=bc.asFloatBuffer();
		tex_coords.put(tex_rect);
		tex_coords.position(0);
		
		bitmap=BitmapFactory.decodeResource(context.getResources(), net.dystopiazero.theglitcher.R.drawable.laughingman);
		bitmap_data=new int[bitmap.getWidth()*bitmap.getHeight()];
		bitmap.getPixels(bitmap_data, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
		buff=IntBuffer.wrap(bitmap_data);
		
		bindTexture();
		
		isReady=true;
	}
	
	private void compileShader()
	{
		Glitcher_Utils.compileShaders(super.v_shade_body, this.f_shader, shader_ids);
	}
	
	private void bindTexture()
	{
		compileShader();
		
		GLES20.glGenTextures(1,texid,0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texid[0]);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buff);
	}
			
	@Override
	public void destroyEffect()
	{
		super.destroyEffect();
		
		GLES20.glDeleteTextures(1,texid,0);
	}
		
	@Override
	protected String getFragShaderSource()
	{
		return super.f_shade_head+super.f_shade_body;
	}
			
	@Override
	public String getEffectName()
	{
		return effectName;
	} 
			
	@Override
	public String getEffectShortName()
	{
		return "laughing";
	}
	
	@Override
	public int getNotificationID()
	{
		return R.drawable.laughingnotification;
	}
			
	@Override
	public void renderAdditionalAssets()
	{
		if (!isReady)
			buildTexture();
		
		int id=shader_ids[Glitcher_Utils.S_PROG];
		//GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	
		GLES20.glUseProgram(id);
		
		for(int k=0; k<n_faces; k++)
		{
			buildBuffer(faces[k]);
		
			int pos=GLES20.glGetAttribLocation(id, "gPosition");
			GLES20.glEnableVertexAttribArray(pos);
			GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 0, fb);
			
			int tcoord=GLES20.glGetAttribLocation(id, "tex_coord");
			GLES20.glEnableVertexAttribArray(tcoord);
			GLES20.glVertexAttribPointer(tcoord, 2, GLES20.GL_FLOAT, true, 0, tex_coords);
			
			int noisetexloc=GLES20.glGetUniformLocation(id,"gLaughing");
			GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texid[0]);
			GLES20.glUniform1i(noisetexloc,3);
			
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
			GLES20.glDisableVertexAttribArray(pos);
			GLES20.glDisableVertexAttribArray(tcoord);
		}
			
		//GLES20.glDisable(GLES20.GL_DEPTH_TEST);
	}
	
	private void buildBuffer(float[] face)
	{
		ByteBuffer bb=ByteBuffer.allocateDirect(12*8);
		bb.order(ByteOrder.nativeOrder());
		
		float left=(face[0]*2.0f)-1.0f;
		float right=(face[2]*2.0f)-1.0f;
		
		float hmid=((left-right)/2.0f)+right;
		
		float top=2.0f-(face[3]*2.0f)-1.0f;
		float bottom=2.0f-(face[1]*2.0f)-1.0f;
		
		float vmid=(top-bottom)/2.0f;
		
		left=hmid+vmid;
		right=hmid-vmid;
		
		//Log.d(Glitcher_Utils.LOG_TAG,"buildBuffer ("+left+","+top+","+right+","+bottom+")");
		
		float[] screen_rect={
				left, bottom, 0.0f,
				left, top, 0.0f,
				right, top, 0.0f,
				right, top, 0.0f,
				right, bottom, 0.0f,
				left, bottom, 0.0f
		};
		
		fb=bb.asFloatBuffer();
		fb.put(screen_rect);
		fb.position(0);
	}
}
