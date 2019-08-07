package com.example.travelmantics

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setSupportActionBar(register_tool_bar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()

        register_container.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION


        ViewCompat.setOnApplyWindowInsetsListener(
            register_tool_bar
        ) { v, insets ->
            val layoutParams = register_tool_bar.layoutParams as ConstraintLayout.LayoutParams

            layoutParams.topMargin = insets.systemWindowInsetTop

            register_tool_bar.layoutParams = layoutParams

            insets
        }

        register_button.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser () {

        val email = register_email_edit_text.text.toString()
        val password = register_password_edit_text.text.toString()
        val name = confirm_password_edit_text.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {

            dialog = ProgressDialog(this@RegisterActivity)
            dialog.setTitle("Sing in ")
            dialog.setMessage("Pleas wait for sign in")
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    startActivity(Intent(this, ListActivity::class.java))
                    finish()
                    dialog.dismiss()
                    Toast.makeText(this, "Successfully registered :)", Toast.LENGTH_LONG).show()
                }else {
                    dialog.dismiss()
                    Toast.makeText(this, "Error registering, try again later :(", Toast.LENGTH_LONG).show()
                }
            }
        }else {
            Toast.makeText(this,"Please fill up the Credentials :|", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            android.R.id.home -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }

    }
}
