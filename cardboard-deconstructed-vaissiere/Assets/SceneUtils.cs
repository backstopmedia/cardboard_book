using UnityEngine;
using System;
using System.Collections;

public class SceneUtils : MonoBehaviour {
	
	public static SceneUtils PROXY {
		get {
			if (_this == null) {
				_this = UnityEngine.Object.FindObjectOfType<SceneUtils>();
			}
			if (_this == null) {
				Debug.Log("Creating sceneUtils object");
				var go = new GameObject("SceneUtils");
				_this = go.AddComponent<SceneUtils>();
				go.transform.localPosition = Vector3.zero;
			}
			return _this;
		}
	}
	private static SceneUtils _this = null;
	
	public void runIn(float seconds, Action action) {
		StartCoroutine (_runIn (seconds, action));
	}
	
	private IEnumerator _runIn(float s, Action a) {
		yield return new WaitForSeconds (s);
		a.Invoke ();
	}

	private void test() {
		SceneUtils.PROXY.runIn (1f, () => Debug.Log ("One second later..."));
	}
	
}
