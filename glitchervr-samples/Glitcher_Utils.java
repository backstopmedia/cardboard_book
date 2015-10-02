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

import java.nio.IntBuffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.util.Log;

public class Glitcher_Utils 
{
	public static final String LOG_TAG="glitcher_log";	
	
	public static final int V_SHADE=0, F_SHADE=1, S_PROG=2;
	
	public static final String SAVE_SHARED_FILENAME="net.dystopiazero.theglitcher.sharedprefs";
	public static final String SAVE_LAST_EFFECT="last_effect";
	public static final String SAVE_LAST_MODE="last_mode";
	public static final String SAVE_LAST_CAM_INDEX="last_cam_index";
	
	public static void savePreference(String tag, String val, Context context)
	{
		SharedPreferences prefs=context.getSharedPreferences(SAVE_SHARED_FILENAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor=prefs.edit();
		editor.putString(tag, val);
		editor.commit();
		
		Log.d(LOG_TAG,"Glitcher_Utils,savePreference: saved ("+tag+"): "+val);
	}
	
	public static String getPreference(String tag, Context context)
	{
		SharedPreferences prefs=context.getSharedPreferences(SAVE_SHARED_FILENAME,Context.MODE_PRIVATE);
		String val=prefs.getString(tag, "null");
		
		Log.d(LOG_TAG,"Glitcher_Utils,getPreference: got ("+tag+"): "+val);
		
		return val;
	}
	
	public static boolean compileShaders(String vertex_shader, String fragment_shader, int[] ids)
	{
		if (ids.length<3)
		{
			Log.e(LOG_TAG,"Glitcher_Utils,compileShaders: Buffer too small");
			return false;
		}
		
		ids[V_SHADE]=GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		ids[F_SHADE]=GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
			
		GLES20.glShaderSource(ids[V_SHADE],vertex_shader);
		GLES20.glShaderSource(ids[F_SHADE],fragment_shader);
			
		GLES20.glCompileShader(ids[V_SHADE]);
		GLES20.glCompileShader(ids[F_SHADE]);
			
		int[] info=new int[1];
		GLES20.glGetShaderiv(ids[V_SHADE], GLES20.GL_COMPILE_STATUS, info,0);
		if (info[0]==0)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Utils,compileShaders: Vertex Shader Error: "+GLES20.glGetShaderInfoLog(ids[V_SHADE]));
			GLES20.glDeleteShader(ids[V_SHADE]);
			
			return false;
		}
			
			
		GLES20.glGetShaderiv(ids[F_SHADE], GLES20.GL_COMPILE_STATUS, info,0);
		if (info[0]==0)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Utils,compileShaders: Fragment Shader Error: "+GLES20.glGetShaderInfoLog(ids[F_SHADE]));
			GLES20.glDeleteShader(ids[F_SHADE]);

			return false;
		}
			
		ids[S_PROG]=GLES20.glCreateProgram();
		GLES20.glAttachShader(ids[S_PROG],ids[V_SHADE]);
		GLES20.glAttachShader(ids[S_PROG],ids[F_SHADE]);
		GLES20.glLinkProgram(ids[S_PROG]);
		
		IntBuffer inf=IntBuffer.wrap(info);
		GLES20.glGetProgramiv(ids[S_PROG], GLES20.GL_LINK_STATUS, inf);
		if (info[0]==GLES20.GL_FALSE)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_Utils,compileShaders: Shader Program Link Error: "+GLES20.glGetProgramInfoLog(ids[S_PROG]));
			GLES20.glDeleteProgram(ids[S_PROG]);

			return false;
		}
		
		//Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Utils,compileShaders: Compiled successfully. ("+ids[S_PROG]+")");
		
		return true;
	}
}
