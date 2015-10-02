Shader "Custom/Reflective/Textured Glass" {
	Properties {
		_Color ("Color", Color) = (1,1,1,1)
		_MainTex ("Base (RGB)", 2D) = "white" {}
		_ReflectColor ("Reflection Color", Color) = (1,1,1,0.5)
		_Cube ("Reflection Cubemap", Cube) = "" { }
	}	
	Category {
		Lighting Off
		ZWrite Off
	   	Cull Back
		
	SubShader {

		Tags {"Queue"="Transparent"}
		Blend One SrcColor
	
		CGPROGRAM
			#pragma surface surf BlinnPhong nolightmap
			#pragma target 3.0
         struct Input {
             float2 uv_MainTex;
             float3 worldRefl; };
			
			samplerCUBE _Cube;
			fixed4 _Color;
			fixed4 _ReflectColor;
			sampler2D _MainTex;

			void surf (Input IN, inout SurfaceOutput o) {
				fixed4 c = tex2D (_MainTex, IN.uv_MainTex) * _Color;
				o.Gloss = 1;
				fixed4 reflcol = texCUBE (_Cube, IN.worldRefl);
				o.Emission = (reflcol.rgb *  _ReflectColor.a * _ReflectColor.rgb);
				o.Albedo = _Color.a * c.rgb;
			}
			ENDCG
		}
	}
}