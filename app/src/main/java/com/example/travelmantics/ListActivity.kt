package com.example.travelmantics

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabaseReference : DatabaseReference

    private var isAdmin = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)


        mAuth = FirebaseAuth.getInstance()

     /*   if (mAuth.currentUser == null){
            val loginActivityIntent = Intent(this,LoginActivity::class.java)
            startActivity(loginActivityIntent)
            finish()
        }*/

       val userId = mAuth.uid

        mDatabaseReference = FirebaseDatabase.getInstance().reference.child("travel_deals")


        val    mAdministratorsRef = FirebaseDatabase.getInstance().reference.child("administrators")
            .child(userId!!)

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

        deals_list.layoutManager = LinearLayoutManager(this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_activity_menu,menu)


        val insertNewDealItem = menu.findItem(R.id.insert_deal)
        insertNewDealItem.isVisible = isAdmin
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when(item?.itemId){
            R.id.sign_out ->{
                   mAuth.signOut()
                finish()
                val loginIntent = Intent(this,LoginActivity::class.java)
                startActivity(loginIntent)
                true
            }
            R.id.insert_deal -> {
              val detailActivityIntent = Intent(this,DetailActivity::class.java)
                startActivity(detailActivityIntent)
                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }

    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<TravelDeal>()
            .setQuery(mDatabaseReference, TravelDeal::class.java)
            .setLifecycleOwner(this).build()

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<TravelDeal, DealViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.deal_list_item, parent, false)

                return DealViewHolder(view)
            }

            override fun onBindViewHolder(holder: DealViewHolder, position: Int, model: TravelDeal) {

                holder.dealTitle.text = model.title
                holder.dealPrice.text = model.price
                holder.dealDescription.text = model.description

                Glide.with(this@ListActivity).load(model.imageUrl).into(holder.dealImage)

                holder.container.setOnClickListener {
                    val intent = Intent(it.context,DetailActivity::class.java)
                    intent.putExtra("deal",model)
                    it.context.startActivity(intent)
                }

            }

        }

        deals_list.adapter = firebaseRecyclerAdapter

    }

    class DealViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {


        val dealImage= itemView.findViewById<RoundedImageView>(R.id.item_deal_image)
        val dealTitle= itemView.findViewById<TextView>(R.id.item_deal_title)
        val dealPrice= itemView.findViewById<TextView>(R.id.item_deal_price)
        val dealDescription= itemView.findViewById<TextView>(R.id.item_deal_description)
        val container = itemView.findViewById<ConstraintLayout>(R.id.list_item_container)

    }

    public fun showMenu(){
        invalidateOptionsMenu()
    }
}
