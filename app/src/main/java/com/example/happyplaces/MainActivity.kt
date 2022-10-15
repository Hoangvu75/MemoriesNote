package com.example.happyplaces

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var placeRecyclerView: RecyclerView
    private lateinit var placeArrayList: ArrayList<Place>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Python.start(AndroidPlatform(this))

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        placeRecyclerView = findViewById(R.id.placeList)
        placeRecyclerView.layoutManager = linearLayoutManager
        placeRecyclerView.setHasFixedSize(true)
        placeArrayList = arrayListOf()
        getPlaceData(placeRecyclerView, placeArrayList)

        setSupportActionBar(toolbarMainActivity)
        fabHappyPlaces.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }

        swipeToDelete(placeRecyclerView, placeArrayList)
        swipeToEdit(placeRecyclerView, placeArrayList)
    }

    private fun getPlaceData(placeRecyclerView: RecyclerView, placeArrayList: ArrayList<Place>) {
        FirebaseDatabase.getInstance().getReference(obj).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                placeArrayList.clear()
                if (snapshot.exists()) {
                    for (placeSnapshot in snapshot.children) {
                        val place = placeSnapshot.getValue(Place::class.java)
                        placeArrayList.add(place!!)
                    }
                    placeRecyclerView.adapter = PlaceAdapter(placeArrayList)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // do nothing
            }
        })
    }

    private fun swipeToDelete(placeRecyclerView: RecyclerView, placeArrayList: ArrayList<Place>) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Are you sure?")
                    .setMessage("You want to delete this place?")
                    .setPositiveButton("Delete") { _, _ ->
                        val position = viewHolder.adapterPosition
                        val place = placeArrayList[position]
                        FirebaseDatabase.getInstance().getReference(obj).child(place.id.toString()).removeValue()
                        FirebaseStorage.getInstance().getReference(obj).child(place.id.toString()).delete()
                        placeArrayList.removeAt(position)
                        placeRecyclerView.adapter?.notifyItemRemoved(position)
                    }.setNegativeButton("Cancel") { _, _ ->
                        placeRecyclerView.adapter?.notifyItemChanged(viewHolder.adapterPosition)
                        placeRecyclerView.adapter?.notifyDataSetChanged()
                    }.create().show()
            }
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.parseColor("#ff6f69"))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .addSwipeLeftLabel("Delete")
                    .setSwipeLeftLabelTextSize(0, 100f)
                    .setSwipeLeftLabelTypeface(Typeface.DEFAULT_BOLD)
                    .setSwipeLeftLabelColor(Color.WHITE)
                    .addCornerRadius(0, 50)
                    .create()
                    .decorate()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(placeRecyclerView)
    }

    private fun swipeToEdit(placeRecyclerView: RecyclerView, placeArrayList: ArrayList<Place>) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val place = placeArrayList[viewHolder.adapterPosition]
                val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
                intent.putExtra("id", place.id)
                startActivity(intent)
                placeRecyclerView.adapter?.notifyDataSetChanged()
                placeRecyclerView.adapter?.notifyItemChanged(viewHolder.adapterPosition)
            }
             override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                 RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                     .addSwipeRightBackgroundColor(Color.parseColor("#54b2a9"))
                     .addSwipeRightActionIcon(R.drawable.ic_baseline_edit_24)
                     .addSwipeRightLabel("Edit")
                     .setSwipeRightLabelTextSize(0, 100f)
                     .setSwipeRightLabelTypeface(Typeface.DEFAULT_BOLD)
                     .setSwipeRightLabelColor(Color.WHITE)
                     .addCornerRadius(0, 50)
                     .create()
                     .decorate()
                 super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
             }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(placeRecyclerView)
    }
}