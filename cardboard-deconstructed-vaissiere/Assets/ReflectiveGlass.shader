 Shader "Custom/Reflective/Glass" {
	Properties {
		_ReflectColor ("Reflection Color", Color) = (1,1,1,0.5)
		_Cube ("Reflection Cubemap", Cube) = "" { }
	}
	
	Category {
		Lighting Off
		ZWrite Off
	   	Cull Back
			
	SubShader {

		Tags {"Queue"="Transparent"}
		Blend One One
	
		CGPROGRAM
			// This is a surface shader
			#pragma surface surf BlinnPhong nolightmap
			#pragma target 3.0
         struct Input {
             float3 worldRefl; };
			
			samplerCUBE _Cube;
			fixed4 _ReflectColor;

			void surf (Input IN, inout SurfaceOutput o) {
				o.Albedo = 0;
				o.Gloss = 1;
				fixed4 reflcol = texCUBE (_Cube, IN.worldRefl);
				o.Emission = (reflcol.rgb *  _ReflectColor.a * _ReflectColor.rgb);
			}
			ENDCG
		}
	}
}