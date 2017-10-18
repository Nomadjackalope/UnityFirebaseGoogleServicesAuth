using UnityEngine;
using UnityEngine.UI;

public class MainActivity : MonoBehaviour {

	GradleTestWrapper test;

	// Use this for initialization
	void Start () {
		test = new GradleTestWrapper();
			
		InitializeFirebase();
	}

	public void click() {
		Debug.Log("MA| clicked");

		test.startUpGoogleSignIn();
		test.signIn();

		tokenAcquired = false;
	}

	int count = -1;
	bool tokenAcquired = true;
	string token;

	void FixedUpdate() {
		if(count > 0 && count % 100 == 0 && !tokenAcquired) {
			Debug.Log("MA| trying for token");
			token = test.getToken();
			if(token != "") {
				tokenAcquired = true;
				Debug.Log("MA| token acquired: " + token);
				// Once we have the token get the user into Firebase
				authenticateFirebase(token, null);
			}
		}
		count++;
	}

	//------------- Firebase ----------------

	private Firebase.Auth.FirebaseAuth auth;
	private Firebase.Auth.FirebaseUser user;

	private string displayName;
	private string emailAddress;

	void InitializeFirebase() {
		auth = Firebase.Auth.FirebaseAuth.DefaultInstance;
		auth.StateChanged += AuthStateChanged;
		AuthStateChanged(this, null);
	}

	void AuthStateChanged(object sender, System.EventArgs eventArgs) {
		if (auth.CurrentUser != user) {
			bool signedIn = user != auth.CurrentUser && auth.CurrentUser != null;
			if (!signedIn && user != null) {
				Debug.Log("Signed out " + user.UserId);
			}
			user = auth.CurrentUser;
			if (signedIn) {
				Debug.Log("Signed in " + user.UserId);
				displayName = user.DisplayName ?? "";
				emailAddress = user.Email ?? "";
				Debug.Log("Signed in email: " + emailAddress);
			}
		}
	}

	void authenticateFirebase(string googleIdToken, string googleAccessToken) {
		Firebase.Auth.Credential credential =
		Firebase.Auth.GoogleAuthProvider.GetCredential(googleIdToken, googleAccessToken);
		auth.SignInWithCredentialAsync(credential).ContinueWith(task => {
		if (task.IsCanceled) {
			Debug.LogError("SignInWithCredentialAsync was canceled.");
			return;
		}
		if (task.IsFaulted) {
			Debug.LogError("SignInWithCredentialAsync encountered an error: " + task.Exception);
			return;
		}

		Firebase.Auth.FirebaseUser newUser = task.Result;
		Debug.LogFormat("User signed in successfully: {0} ({1})",
			newUser.DisplayName, newUser.UserId);
		});
	}

	public void signOut() {
		auth.SignOut();
	}

	void onDestroy() {
		auth.StateChanged -= AuthStateChanged;
		auth = null;
	}
}
