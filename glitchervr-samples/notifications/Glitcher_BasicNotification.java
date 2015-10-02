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

import android.util.Log;

public class Glitcher_BasicNotification implements Glitcher_Notification
{
	private double ttl;
	private String text;
	
	public Glitcher_BasicNotification(String text, double ttl_in_millis)
	{
		this.text=new String(text);
		ttl=ttl_in_millis;		
	}
	
	public double getTTL()
	{
		return ttl;
	}
	
	public void draw(double time)
	{
		
	}
	
	public void build()
	{
		
	}


	public void cleanup() 
	{
		
	}

	@Override
	public double getStartTime() 
	{
		return 0;
	}

	@Override
	public double getEndTime() 
	{
		return 0;
	}
	
	public boolean isReady()
	{
		return false;
	}
}
