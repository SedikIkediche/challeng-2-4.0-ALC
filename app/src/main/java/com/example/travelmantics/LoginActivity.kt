package com.example.travelmantics

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 7
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        login_container.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                             View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                              View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

        google_sign_in_button.setOnClickListener {
            signIn()
        }


        sign_up_text_view.setOnClickListener {
            val registerIntent = Intent(this,RegisterActivity::class.java)
            startActivity(registerIntent)
            finish()
        }

        login_button.setOnClickListener {
            login()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            dialog = ProgressDialog(this@LoginActivity)
            dialog.setTitle("Sing in ")
            dialog.setMessage("Pleas wait for sign in")
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                Toast.makeText(this,"Google sign in successful",Toast.LENGTH_SHORT).show()
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
               Toast.makeText(this,"Google sign in failed",Toast.LENGTH_SHORT).show()
                // ...
            }

        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {

        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this," signInWithCredential:success",Toast.LENGTH_SHORT).show()
                    val user = mAuth.currentUser
                    dialog.dismiss()
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this,"Auth Failed",Toast.LENGTH_LONG).show()
                    dialog.dismiss()

                }

                // ...
            }
    }

    private fun login () {
        val email = email_edit_text.text.toString()
        val password = password_edit_text.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {

            dialog = ProgressDialog(this@LoginActivity)
            dialog.setTitle("Sing in ")
            dialog.setMessage("Pleas wait for sign in")
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            this.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener ( this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, ListActivity::class.java))
                    Toast.makeText(this, "Successfully Logged in :)", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                } else {
                    dialog.dismiss()
                    Toast.makeText(this, "Error Logging in :(", Toast.LENGTH_SHORT).show()
                }
            }

        }else {
            Toast.makeText(this, "Please fill up the Credentials :|", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(user : FirebaseUser?) {

            if(user != null){
                val listActivityIntent = Intent(this,ListActivity::class.java)
                startActivity(listActivityIntent)
                finish()
            }
        }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }


    private fun signIn() {
        mGoogleSignInClient.signOut()
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
}
