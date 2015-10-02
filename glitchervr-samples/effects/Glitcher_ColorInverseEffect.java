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

import net.dystopiazero.theglitcher.R;

public class Glitcher_ColorInverseEffect extends Glitcher_BasicEffect 
{
	protected String f_shade_body=
		"void main()\n"+
		"{\n"+
			"vec4 color=texture2D(gTexture,gTexCoord.xy);\n"+
			"gl_FragColor=vec4(1.0-color.r,1.0-color.g,1.0-color.b,1.0);\n"+
		"}";
	
	protected String effectName="Color Inverse";
	
	public Glitcher_ColorInverseEffect()
	{	
	}
	
	@Override
	protected String getFragShaderSource()
	{
		return super.f_shade_head+this.f_shade_body;
	}
	
	@Override
	public String getEffectName()
	{
		return effectName;
	} 
	
	@Override
	public String getEffectShortName()
	{
		return "inverse";
	}
	
	@Override
	public int getNotificationID()
	{
		return R.drawable.inversenotification;
	}
}
