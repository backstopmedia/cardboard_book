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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import net.dystopiazero.theglitcher.Glitcher_Utils;
import android.util.Log;

public class Glitcher_NotificationScheduler 
{
	private static final Glitcher_NotificationScheduler __notif_sys=new Glitcher_NotificationScheduler();
	
	private Vector<Glitcher_Notification> notifications;
	
	private Glitcher_NotificationScheduler()
	{
		notifications=new Vector<Glitcher_Notification>();
	}
	
	public static Glitcher_NotificationScheduler getInstance()
	{
		return __notif_sys;
	}
	
	public void add(Glitcher_Notification notification)
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_NotificationScheduler,add: Adding new notification.");
		
		notifications.add(notification);
	}
	
	public void clear()
	{
		notifications.clear();
		
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_NotificationScheduler,clear");
	}
	
	public void clearDeadNotifications()
	{
		double curtime=System.nanoTime()/1000000.0;
		
		boolean found=true;
		while(found)
		{
			found=false;
			
			for(Iterator<Glitcher_Notification> i=notifications.iterator(); i.hasNext();)
			{
				try
				{
					Glitcher_Notification e=i.next();
				
					if (e.getEndTime()<=curtime)
					{
						Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_NotificationScheduler,clearDeadNotifications: Dead notification found.");
					
						e.cleanup();
						notifications.remove(e);
						found=true;
						break;
					}
				}
				
				catch(Exception e)
				{
					found=false;
					Log.w(Glitcher_Utils.LOG_TAG,"Glitcher_NotificationScheduler,clearDeadNotifications: "+e.getMessage());
				}
			}
		}
	}
	
	public void run()
	{
		double curtime=System.nanoTime()/1000000.0;
		
		for(int k=0; k<notifications.size(); k++)
		{
			Glitcher_Notification e=notifications.get(k);
			double time=e.getEndTime()-curtime;
			
			e.draw(time);
		}
	}
	
	public Iterator<Glitcher_Notification> iterator()
	{
		return notifications.iterator();
	}
}
