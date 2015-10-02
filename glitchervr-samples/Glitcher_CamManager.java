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

import java.util.Vector;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

public class Glitcher_CamManager 
{
	private static final Glitcher_CamManager __cam_manager=new Glitcher_CamManager();
	
	private boolean isInit;
	private Glitcher_Cam cam;
	private int activeCam;
	private int totalCams;
	
	private Glitcher_CamManager()
	{
		cam=null;
		isInit=false;
		activeCam=-1;
		totalCams=0;
		
		init();
	}
	
	public static Glitcher_CamManager getInstance()
	{
		return __cam_manager;
	}
	
	public void init()
	{
		int num_cams=Camera.getNumberOfCameras();
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamManager,init: Found "+num_cams+" cameras");
		
		if (num_cams<=0)
		{
			isInit=false;
			return;
		}
		
		activeCam=0;
		totalCams=num_cams;
		
		isInit=true;
	}
	
	public boolean isReady()
	{
		return isInit;
	}
	
	public Glitcher_Cam getActiveCam()
	{
		return cam;
	}
	
	public int getActiveCamIndex()
	{
		return activeCam;
	}
	
	public Glitcher_Cam activateCamAtIndex(Glitcher_CamSurface surf, Context context, int index)
	{
		if (index<0 || index>=totalCams)
			return cam;
		
		if (cam!=null && cam.isActive())
			cam.stop();
		
		activeCam=index;
		start(surf,context);
		
		return cam;
	}
	
	public Glitcher_Cam activateNext(Glitcher_CamSurface surf, Context context)
	{
		if (cam!=null && cam.isActive())
			cam.stop();
		
		activeCam=(activeCam+1)%totalCams;
		start(surf, context);
		
		return cam;
	}
	
	public Glitcher_Cam activatePrevious(Glitcher_CamSurface surf, Context context)
	{
		if (cam!=null && cam.isActive())
			cam.stop();
		
		activeCam=(totalCams+activeCam-1)%totalCams;
		start(surf, context);
		
		return cam;
	}
	
	public void stop()
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamManager,stop: Stopping camera "+activeCam+"...");
		
		if (cam==null)
			return;
		
		if (cam.isActive())
			cam.stop();
		
		cam=null;
	}
	
	public void start(Glitcher_CamSurface surf, Context context)
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_CamManager,start: Starting camera "+activeCam+"...");
		
		if (cam!=null)
			cam.stop();
		
		cam=new Glitcher_Cam(surf,activeCam,context);
		
		if (!cam.isActive())
			cam.start();
	}
	
	public void start()
	{
		if (cam!=null && !cam.isActive())
			cam.start();
	}
	
	public void restart()
	{
		stop();
		start();
	}
}
