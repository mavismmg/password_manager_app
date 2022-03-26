package com.example.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(applicationContext)

        verifyIfUserIsLoggedIn()

        button_loginActivity_btt.setOnClickListener {
            userLogin()
        }

        facebook_login.setOnClickListener {
            facebookLogin()
        }

        // googleLogin() implementation missing.
//        google_login.setOnClickListener {
//            googleLogin()
//        }

        biometric_login.setOnClickListener {
            biometricLogin()
        }

        dont_have_account_login.setOnClickListener {
            userHaveAnAccount()
        }
    }

    private fun biometricLogin() : Unit {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this@LoginActivity, executor, object: BiometricPrompt.AuthenticationCallback() {
            @SuppressLint("SetTextI18n")
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                biometric_login.text = "Authentication Error: $errString"
                Toast.makeText(this@LoginActivity, "Authentication Error: $errString", Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("SetTextI18n")
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                biometric_login.text = "Authentication Successful"
                Toast.makeText(this@LoginActivity, "Authentication Successful", Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("SetTextI18n")
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                biometric_login.text = "Authentication Failed"
                Toast.makeText(this@LoginActivity, "Authentication Failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setNegativeButtonText("button_loginActivity_btt")
            .build()
    }

    private fun userLogin() : Unit {
        val email = username_loginActivity_txt.text.toString()
        //val email = email_loginActivity_txt.text.toString()
        val password = password_loginActivity_txt.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please provide a e-mail and password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Log.d("Login", "Successfully logged in: ${it.result.user!!.uid}")
                    startActivity(Intent(this, AppActivity::class.java))
                } else {
                    return@addOnCompleteListener
                }
            }
            .addOnFailureListener {
                Log.d("Login", "Failed to log in: ${it.message}")

                if (it.message == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                    Toast.makeText(this, "E-mail or password is wrong", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "Invalid e-mail", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun facebookLogin() {
        val callBackManager = CallbackManager.Factory.create()

        facebook_login.setPermissions("email", "public_profile")
        facebook_login.registerCallback(
            callBackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d("Facebook", "facebook:onSuccess:$result")
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Log.d("Facebook", "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.d("Facebook", "facebook:onError", error)
                }
            })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)

        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Log.d("Facebook", "signInWithCredential:success")
                    val user = FirebaseAuth.getInstance().currentUser
                    Log.d("Facebook", user.toString())
                    startActivity(Intent(this, AppActivity::class.java))
                } else {
                    Log.w("Facebook", "signInWithCredential:failure", it.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun googleLogin() {
        val gso : GoogleSignInOptions? = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //.requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = gso?.let { GoogleSignIn.getClient(this, it) }

        val account : GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

        if (account != null) {
            Log.d("Google", "User is already logged in")
            startActivity(Intent(this, AppActivity::class.java))
        }

        // Still Implementing
    }

    private fun verifyIfUserIsLoggedIn() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            Log.d("Facebook", "User is already logged in using facebook login method")
            startActivity(Intent(this, AppActivity::class.java))
        } else {
            Log.d("Facebook", "User is not logged with facebook account")
        }
    }

    private fun userHaveAnAccount() : Unit {
        Log.d("LoginActivity", "User already have an account")
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}