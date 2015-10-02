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

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Glitcher_TextRender 
{
	private class Rectangle
	{
		public int x,y,w,h;
		public char letter;
		
		public Rectangle(char letter, int x, int y, int w, int h)
		{
			this.letter=letter;
			this.x=x;
			this.y=y;
			this.h=h;
			this.w=w;
		}
	}
	
	private HashMap<Character, Rectangle> fontmap;
	
	private Bitmap fonts;
	private Context context;
	
	public Glitcher_TextRender(Context context)
	{
		fonts=BitmapFactory.decodeResource(context.getResources(), R.drawable.fontmap);
		this.context=context;
		
		initFontMap();
	}
	
	private void initFontMap()
	{
		fontmap=new HashMap<Character, Rectangle>();
		
		fontmap.put('A',new Rectangle('A',7,2,31,20));
		fontmap.put('B',new Rectangle('B',32,2,57,20));
		fontmap.put('C',new Rectangle('C',58,2,82,20));
		fontmap.put('D',new Rectangle('D',83,2,109,20));
		fontmap.put('E',new Rectangle('E',109,2,134,20));
		fontmap.put('F',new Rectangle('F',134,2,160,20));
		fontmap.put('G',new Rectangle('G',160,2,186,20));
		fontmap.put('H',new Rectangle('H',186,2,212,20));
		fontmap.put('I',new Rectangle('I',212,2,218,20));
		fontmap.put('J',new Rectangle('J',218,2,243,20));
		fontmap.put('K',new Rectangle('K',243,2,269,20));
		fontmap.put('L',new Rectangle('L',269,2,295,20));
		fontmap.put('M',new Rectangle('M',295,2,324,20));
		fontmap.put('N',new Rectangle('N',324,2,349,20));
		fontmap.put('O',new Rectangle('O',349,2,375,20));
		fontmap.put('P',new Rectangle('P',375,2,401,20));
		fontmap.put('Q',new Rectangle('Q',401,2,426,20));
		fontmap.put('R',new Rectangle('R',426,2,452,20));
		fontmap.put('S',new Rectangle('S',452,2,478,20));
		fontmap.put('T',new Rectangle('T',478,2,502,20));
		fontmap.put('U',new Rectangle('U',502,2,528,20));
		fontmap.put('V',new Rectangle('V',528,2,557,20));
		fontmap.put('W',new Rectangle('W',557,2,586,20));
		fontmap.put('X',new Rectangle('X',586,2,612,20));
		fontmap.put('Y',new Rectangle('Y',612,2,637,20));
		fontmap.put('Z',new Rectangle('Z',637,2,662,20));
		fontmap.put('1',new Rectangle('1',662,2,688,20));
		fontmap.put('2',new Rectangle('2',688,2,693,20));
		fontmap.put('3',new Rectangle('3',693,2,720,20));
		fontmap.put('4',new Rectangle('4',720,2,745,20));
		fontmap.put('5',new Rectangle('5',745,2,796,20));
		fontmap.put('6',new Rectangle('6',796,2,822,20));
		fontmap.put('7',new Rectangle('7',822,2,847,20));
		fontmap.put('8',new Rectangle('8',847,2,873,20));
		fontmap.put('9',new Rectangle('9',873,2,899,20));
		fontmap.put('0',new Rectangle('0',899,2,925,20));
		fontmap.put('~',new Rectangle('~',925,2,942,20));
		fontmap.put('!',new Rectangle('!',942,2,948,20));
		fontmap.put('@',new Rectangle('@',948,2,973,20));
		fontmap.put('#',new Rectangle('#',973,2,999,20));
		fontmap.put('$',new Rectangle('$',999,2,1025,20));
		fontmap.put('%',new Rectangle('%',1025,2,1045,20));
		fontmap.put('^',new Rectangle('^',1045,2,1057,20));
		fontmap.put('&',new Rectangle('&',1057,2,1082,20));
		fontmap.put('*',new Rectangle('*',1082,2,1095,20));
		fontmap.put('(',new Rectangle('(',1095,2,1103,20));
		fontmap.put(')',new Rectangle(')',1103,2,1111,20));
		fontmap.put('-',new Rectangle('-',1111,2,1133,20));
		fontmap.put('=',new Rectangle('=',1133,2,1153,20));
		fontmap.put('+',new Rectangle('+',1153,2,1175,20));
		fontmap.put('[',new Rectangle('[',1175,2,1184,20));
		fontmap.put(']',new Rectangle(']',1184,2,1192,20));
		fontmap.put('{',new Rectangle('{',1192,2,1201,20));
		fontmap.put('}',new Rectangle('}',1201,2,1210,20));
		fontmap.put(';',new Rectangle(';',1210,2,1216,20));
		fontmap.put(':',new Rectangle(':',1216,2,1222,20));
		fontmap.put('\'',new Rectangle('\'',1222,2,1228,20));
		fontmap.put('\"',new Rectangle('\"',1228,2,1237,20));
		fontmap.put(',',new Rectangle(',',1237,2,1243,20));
		fontmap.put('.',new Rectangle('.',1243,2,1248,20));
		fontmap.put('<',new Rectangle('<',1248,2,1260,20));
		fontmap.put('>',new Rectangle('>',1260,2,1273,20));
		fontmap.put('/',new Rectangle('/',1273,2,1290,20));
		fontmap.put('?',new Rectangle('?',1290,2,1310,20));
		fontmap.put('_',new Rectangle('_',1310,2,1333,20));
		fontmap.put('\\',new Rectangle('\\',1333,2,1351,20));
		fontmap.put(' ',new Rectangle(' ',1351,2,1371,20));
	}
	
	public Bitmap getBitmap(String text)
	{
		if (text==null || text.length()<=0)
			text=" ";
		
		text=text.toUpperCase();
		int length=calculateTextLength(text);
		
		Bitmap tbit=Bitmap.createBitmap(length+10,26,Bitmap.Config.ARGB_8888);
		char[] characters=text.toCharArray();
		
		int xoffset=5;
		for(int k=0; k<text.length(); k++)
		{
			Rectangle rect=fontmap.get(text.charAt(k));
			int char_length=rect.w-rect.x;
			
			int[] buff=new int[char_length*20];
			fonts.getPixels(buff, 0, char_length, rect.x, rect.y, char_length, 20);
			tbit.setPixels(buff, 0, char_length, xoffset, 3, char_length, 20);
			
			xoffset+=char_length;
		}
		
		return tbit;
	}
	
	private int calculateTextLength(String text)
	{
		int length=0;
		
		for(int k=0; k<text.length(); k++)
		{
			char letter=text.charAt(k);
			
			Rectangle rect=fontmap.get(letter);
			length+=rect.w-rect.x;
		}
		
		return length;
	}
}
