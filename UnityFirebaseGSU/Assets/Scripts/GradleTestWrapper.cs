using UnityEngine;

public class GradleTestWrapper : MonoBehaviour {

	public static GradleTestWrapper instance;

	private AndroidJavaObject mTestObj;
	private AndroidJavaClass unityClass;
	private AndroidJavaObject unityActivity;

	public GradleTestWrapper() {
		if (instance == null)
			instance = this;

		mTestObj = new AndroidJavaObject("com.nomadjackalope.tgemt47lib.GoogleServicesWrapper");
		unityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
		unityActivity = unityClass.GetStatic<AndroidJavaObject>("currentActivity");

	}

	public void startUpGoogleSignIn() {
		Debug.Log("GTW| startUpGoogleSignIn");
		mTestObj.Call("setupGoogleSignIn", unityActivity);
	}

	public void signIn() {
		Debug.Log("GTW| signIn");		
		mTestObj.Call("signIn", unityActivity);
	}

	public string getToken() {
		string token = mTestObj.Call<string>("getToken");
		Debug.Log("GTW| token: " + token);
		return token;
	}
}
