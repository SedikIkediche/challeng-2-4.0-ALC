package com.example.travelmantics

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_detail.*




class DetailActivity : AppCompatActivity() {

    private val PICTURE_REQUEST = 45

    private lateinit var mDeal : TravelDeal

    private lateinit var mStorageReference : StorageReference

    private lateinit var mDatabaseReference : DatabaseReference
    private var isAdmin = false
    private lateinit var dialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setSupportActionBar(detail_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        detail_activity_container.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        ViewCompat.setOnApplyWindowInsetsListener(
            detail_toolbar
        ) { v, insets ->
            val layoutParams = detail_toolbar.layoutParams as ConstraintLayout.LayoutParams

            layoutParams.topMargin = insets.systemWindowInsetTop

            detail_toolbar.layoutParams = layoutParams

            insets
        }

        val    mAdministratorsRef = FirebaseDatabase.getInstance().reference.child("administrators")
            .child(FirebaseAuth.getInstance().uid!!)

        mAdministratorsRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                isAdmin = true
                invalidateOptionsMenu()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

        mStorageReference = FirebaseStorage.getInstance().reference.child("deals_pictures")

        mDatabaseReference = FirebaseDatabase.getInstance().reference.child("travel_deals")

        val detailActivityIntent = intent
        var deal: TravelDeal? = detailActivityIntent.getParcelableExtra("deal")
        if (deal == null) {
            deal = TravelDeal()
        }
        this.mDeal = deal

        upload_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "insert picture"), PICTURE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICTURE_REQUEST && resultCode == RESULT_OK && data != null){

            val file = data.data
            loadImage(file)
            val reference = mStorageReference.child(file.lastPathSegment)
             dialog = ProgressDialog(this@DetailActivity)
            dialog.setTitle("Upload image")
            dialog.setMessage("Wait for uploading")
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            reference.putFile(file).addOnSuccessListener { taskSnapshot ->
                reference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    mDeal.imageUrl = downloadUrl
                    mDeal.imageName = taskSnapshot.storage.path

                    dialog.dismiss()
                    Toast.makeText(this@DetailActivity, "uploaded sucssefully", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_activity_menu,menu)

        val saveMenuItem = menu.findItem(R.id.save_deal)
        val deleteMenuItem = menu.findItem(R.id.delete_deal)

            saveMenuItem.isVisible = isAdmin
            deleteMenuItem.isVisible = isAdmin
            textViewVisibilty(isAdmin)
            editTextVisibilty(isAdmin)


        return true
    }

    @SuppressLint("RestrictedApi")
    private fun editTextVisibilty(isAdmin: Boolean) {
        if(isAdmin){
            title_edit_text.visibility = View.VISIBLE
            price_edit_text.visibility = View.VISIBLE
            description_edit_text.visibility = View.VISIBLE
            upload_button.visibility = View.VISIBLE
            title_edit_text.setText(mDeal.title)
            price_edit_text.setText(mDeal.price)
            description_edit_text.setText(mDeal.description)
            if (mDeal.imageUrl != ""){
                Glide.with(this).load(mDeal.imageUrl).into(deal_image)
            }

        }else{
            title_edit_text.visibility = View.GONE
            price_edit_text.visibility = View.GONE
            description_edit_text.visibility = View.GONE
            upload_button.visibility = View.GONE
        }
    }

    private fun textViewVisibilty(isAdmin: Boolean) {
        if (isAdmin){
            deal_title.visibility = View.GONE
            deal_price.visibility = View.GONE
            deal_description.visibility = View.GONE
            favorit_image_view.visibility = View.GONE
        }else{
            deal_title.visibility = View.VISIBLE
            deal_price.visibility = View.VISIBLE
            deal_description.visibility = View.VISIBLE
            favorit_image_view.visibility = View.VISIBLE
            deal_title.text = mDeal.title
            deal_price.text = mDeal.price
            deal_description.text = mDeal.description
            Glide.with(this).load(mDeal.imageUrl).into(deal_image)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            R.id.save_deal ->{
                saveDeal()
                return true
            }R.id.delete_deal ->{
                deleteDeal()
            return true
            }else ->{
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun saveDeal() {

        if (title_edit_text.text.toString() == "" || price_edit_text.text.toString() == "" || description_edit_text.text.toString() == "") {
            Toast.makeText(this, "Please enter all deal informations", Toast.LENGTH_SHORT).show()
            return
        }
        mDeal.title = title_edit_text.text.toString()
        mDeal.price = price_edit_text.text.toString()
        mDeal.description = description_edit_text.text.toString(

        )
        if (mDeal.id == "") {
            mDeal.id = mDatabaseReference.push().key.toString()
            mDatabaseReference.child(mDeal.id).setValue(mDeal)


        } else {
            mDatabaseReference.child(mDeal.id).setValue(mDeal)
        }
        Toast.makeText(this, "Deal saved", Toast.LENGTH_SHORT).show()
        clean()
        backTolist()
    }

    private fun deleteDeal() {
        if (title_edit_text.text.toString() == "" || price_edit_text.text.toString() == "" || description_edit_text.text.toString() == "") {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show()
            return
        }

           mDatabaseReference.child(mDeal.id).removeValue()

        if (mDeal.imageName != null && mDeal.imageName.isNotEmpty()) {
            val reference = FirebaseStorage.getInstance().reference.child(mDeal.imageName)
            reference.delete().addOnSuccessListener {
                Toast.makeText(
                    this@DetailActivity,
                    "deleted nicely",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { Toast.makeText(this@DetailActivity, "non deleted", Toast.LENGTH_SHORT).show() }
        }
        Toast.makeText(this, "Deal deleted", Toast.LENGTH_SHORT).show()
        backTolist()
    }

    private fun backTolist() {
        finish()
    }

    private fun clean() {
        title_edit_text.setText("")
        price_edit_text.setText("")
        description_edit_text.setText("")
        title_edit_text.requestFocus()
    }


    private fun loadImage(file: Uri?) {
        Glide.with(this).load(file).into(deal_image)
    }

}
