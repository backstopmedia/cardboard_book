// This is where you'll find your shader in the menu
Shader "Custom/Unlit/Alpha" { 
	Properties {
		_Color ("Color Tint", Color) = (1,1,1,1)
		_MainTex ("Tecture (RGB) Alpha (A)", 2D) = "white"
	}
	Category {
	   Lighting Off // This material will not be affected by scene lights
	   ZWrite Off // This material has alpha and must not write to depth buffer
	   Cull Back // We do not render back faces
	   Blend SrcAlpha OneMinusSrcAlpha // Blending operation
	   Tags {Queue=Transparent} // Transparent queue is on top
	   Fog {Mode Off} // Use Mode On if you want your material to receive fog colour
	   // This instruction reserves a GPU pipe
	   // You can have several that will run in parallel
	   SubShader { 
	   		// You can have several pass for complex rendering
            Pass {
            	// SetTexture is the simpler instruction
	           SetTexture [_MainTex] {
	                constantColor [_Color] // Use color
	                combine texture + constant  // Add color
					//combine texture * constant  // You can multiply instead
                }
            }
        } 
    }
}