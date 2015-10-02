Shader "Custom/Unlit/Selflit texture" {
	Properties {
		_Color ("Color", Color) = (1,1,1,1)
		_MainTex ("Base (RGB)", 2D) = "white" {}
		_Ambiant ("Ambiant", Range(0,1)) = 0.1
	}
	
	Category {
		Lighting Off
	   	Cull Back
	   	
	SubShader {
		Pass {
	
		CGPROGRAM
		#pragma vertex vert // This is a vertex shader
        #pragma fragment frag // This is a fragment shader
        #include "UnityCG.cginc"

		// Variables from shader properties
		float _Ambiant; 
        fixed4 _Color;
        sampler2D _MainTex;

		// Internal variables
        struct v2f {
            float4 pos : SV_POSITION;
            float2 uv : TEXCOORD0;
        };
        float4 _MainTex_ST;

		// Apply texture according to normals and UV map
		//This is the place to apply deformations or alike
        v2f vert (appdata_base v)
        {
            v2f o;
            o.pos = mul (UNITY_MATRIX_MVP, v.vertex);
            o.uv = TRANSFORM_TEX (v.texcoord, _MainTex);
            return o;
        }

		// Render pixels
		// This is the place to appl blending
		//   and colouring operations
        fixed4 frag (v2f i) : SV_Target
        {
            fixed4 c = tex2D (_MainTex, i.uv);
            return fixed4((c.rgb * _Color.rgb * (c.rgb+_Ambiant)) * _Color.a,1);
        }
		ENDCG
		}
	}
	}
}