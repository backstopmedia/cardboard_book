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

import net.dystopiazero.theglitcher.Glitcher_CamSurface;
import net.dystopiazero.theglitcher.Glitcher_Utils;
import net.dystopiazero.theglitcher.R;
import android.opengl.GLES20;
import android.util.Log;

public class Glitcher_BasicEffect implements Glitcher_Effect
{
	protected String v_shade_body=
			"attribute vec4 gPosition;\n"+
			"attribute vec2 tex_coord;\n"+
			"uniform mat4 mvp;\n"+
			"uniform mat4 mv;\n"+
			"varying vec2 gTexCoord;\n"+
			
			"void main()\n"+
			"{\n"+
				"gl_Position=mvp*gPosition;\n"+
				"gTexCoord=tex_coord;\n"+
			"}";
	
	protected String f_shade_head=
			"#extension GL_OES_EGL_image_external : require\n"+
			"precision lowp float;\n"+
			"varying vec2 gTexCoord;\n"+
			"uniform samplerExternalOES gTexture;\n"+
			"uniform sampler2D gNoise;\n"+
			"uniform vec2 gResolution;\n"+
			"uniform float gTime;\n"+
			
			"uniform int gNumFaces;\n"+
			"uniform vec4 gFace0;\n"+
			"uniform vec4 gFace1;\n"+
			"uniform vec4 gFace2;\n"+
			"uniform vec4 gFace3;\n"+
			"uniform vec4 gFace4;\n"+
			"uniform vec4 gFace5;\n"+
			"uniform vec4 gFace6;\n"+
			"uniform vec4 gFace7;\n"+
			"uniform vec4 gFace8;\n"+
			"uniform vec4 gFace9;\n";
	
	protected String f_shade_body=
			"void main()\n"+
			"{\n"+
			"vec4 color=texture2D(gTexture,gTexCoord.xy);\n"+
			
			"gl_FragColor=color;\n"+
			"}";
	
	protected String effectName="Camera Passthrough";
	
	private int v_shade_id;
	private int f_shade_id;
	private int s_program_id;
	
	private boolean isReady;
			
	public Glitcher_BasicEffect()
	{
		v_shade_id=-1;
		f_shade_id=-1;
		s_program_id=-1;
		
		isReady=false;
	}
	
	public boolean buildEffect()
	{
		int[] ids=new int[3];
		isReady=Glitcher_Utils.compileShaders(getVertShaderSource(),getFragShaderSource(),ids);
		
		v_shade_id=ids[Glitcher_Utils.V_SHADE];
		f_shade_id=ids[Glitcher_Utils.F_SHADE];
		s_program_id=ids[Glitcher_Utils.S_PROG];
		
		return isReady;
	}
	
	public void destroyEffect()
	{
		if (isReady)
		{
			GLES20.glDeleteProgram(s_program_id);
			GLES20.glDeleteShader(v_shade_id);
			GLES20.glDeleteShader(f_shade_id);
		}
	}
	
	public void prerender(Glitcher_CamSurface surf)
	{
	}
	
	public boolean isReady()
	{
		return isReady;
	}
	
	public int getEffectID()
	{
		return s_program_id;
	}
	
	public String getEffectName()
	{
		return effectName;
	}
	
	public String getEffectShortName()
	{
		return "camera";
	}
	
	public int getNotificationID()
	{
		return R.drawable.cameranotification;
	}
	
	protected String getFragShaderSource()
	{
		String f_shade=f_shade_head+f_shade_body;
		
		return f_shade;
	}
	
	protected String getVertShaderSource()
	{
		return v_shade_body;
	}
	
	public void renderAdditionalAssets()
	{
	}
}
