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
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.Vector;

import net.dystopiazero.theglitcher.effects.Glitcher_EffectManager;
import net.dystopiazero.theglitcher.menu.Glitcher_Menu;
import net.dystopiazero.theglitcher.menu.Glitcher_MenuContainer;
import net.dystopiazero.theglitcher.menu.Glitcher_MenuItem;
import net.dystopiazero.theglitcher.menu.Glitcher_MenuItemTest;
import net.dystopiazero.theglitcher.menu.Glitcher_MenuManager;
import net.dystopiazero.theglitcher.notifications.Glitcher_BitmapNotification;
import net.dystopiazero.theglitcher.notifications.Glitcher_Notification;
import net.dystopiazero.theglitcher.notifications.Glitcher_NotificationScheduler;
import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;

public class Glitcher_Inputs 
{
	private static final Glitcher_Inputs __input=new Glitcher_Inputs();
	
	private int[] deviceIds;
	private Vector<Integer> gamepadIds;
	
	private Glitcher_Inputs()
	{
		init();
	}
	
	public static Glitcher_Inputs getInstance()
	{
		return __input;
	}
	
	private void init()
	{
		gamepadIds=new Vector<Integer>();
		
		detectDevices();
	}
	
	private void detectDevices()
	{
		deviceIds = InputDevice.getDeviceIds();
		
		for (int k=0; k<deviceIds.length; k++) 
		{
			 InputDevice dev = InputDevice.getDevice(deviceIds[k]);
			 
			 //Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Inputs,detectDevices: Found device - "+dev.getName()+" id: "+deviceIds[k]);
			 
			 int sources = dev.getSources();

			 if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) || 
				 ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) 
			 {
				 gamepadIds.add(deviceIds[k]);
				 //Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Inputs,detectDevices: Found gamepad - "+dev.getName()); 
			 }
		 }
	}
	
	public boolean handleKeyInput(int code, KeyEvent event, Glitcher_CamSurface surf)
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Inputs,handleKeyInput: "+code+" (id: "+event.getDeviceId());
		
		Glitcher_EffectManager em=Glitcher_EffectManager.getInstance();
		
		if (isGamepad(event.getDeviceId()))
		{
			if (code==KeyEvent.KEYCODE_BUTTON_B || 
					code==KeyEvent.KEYCODE_DPAD_RIGHT ||
					code==KeyEvent.KEYCODE_BUTTON_2)
			{
				em.next();
				
				Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
				ns.clear();
				Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),em.getActiveEffect().getNotificationID(), 2500.0);
				ns.add(bn);
				
				return true;
			}
		
			if (code==KeyEvent.KEYCODE_BUTTON_A || 
					code==KeyEvent.KEYCODE_DPAD_LEFT ||
					code==KeyEvent.KEYCODE_BUTTON_1) 
			{
				em.prev();
				
				Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
				ns.clear();
				Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),em.getActiveEffect().getNotificationID(), 2500.0);
				ns.add(bn);
				
				return true;
			}
		}
		
		if (code==KeyEvent.KEYCODE_VOLUME_UP || code==KeyEvent.KEYCODE_HEADSETHOOK)
		{
			Glitcher_Speech speech=Glitcher_Speech.getInstance();
			speech.activateSpeech();
			
			return true;
		}
		
		if (code==KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			surf.toggleVRMode();
			
			return true;
		}
		
		return false;
	}
	
	public void handleMagnetEvent()
	{
		Glitcher_Speech speech=Glitcher_Speech.getInstance();
		if (speech.isReady())
			speech.activateSpeech();
	}
	
	private boolean isGamepad(int devId)
	{
		for(Iterator<Integer> i=gamepadIds.iterator(); i.hasNext();)
		{
			Integer pad=i.next();
			
			if (pad==devId)
				return true;
		}
		
		return false;
	}
}
