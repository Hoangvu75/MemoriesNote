package com.example.happyplaces

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.text.LineBreaker
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PlaceAdapter(private val placeList: ArrayList<Place>):RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent, false))
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val currentItem = placeList[position]
        holder.title.text = currentItem.title
        holder.description.text = currentItem.description

        val localFile = File.createTempFile("tempImage", "jpg")
        FirebaseStorage.getInstance().getReference(obj).child(currentItem.id.toString()).getFile(localFile).addOnSuccessListener {
            holder.itemView.findViewById<ImageView>(R.id.ivImage).setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
        }

        if(position % 2 == 0) {
            holder.item.setCardBackgroundColor(Color.parseColor("#FFDFF8"))
        } else {
            holder.item.setCardBackgroundColor(Color.parseColor("#C1E2FF"))
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    inner class PlaceViewHolder(placeView: View): RecyclerView.ViewHolder(placeView), View.OnLongClickListener, View.OnClickListener {
        val title: TextView = placeView.findViewById(R.id.tvTitle)
        val description: TextView = placeView.findViewById(R.id.tvDescription)
        val item: CardView = placeView.findViewById(R.id.cvItem)

        init {
            item.setOnLongClickListener(this)
            item.setOnClickListener(this)
        }

        @SuppressLint("ClickableViewAccessibility", "SetTextI18n", "InflateParams")
        override fun onLongClick(p0: View?): Boolean {
            item.setCardBackgroundColor(Color.parseColor("#FFF200"))

            val ivImage = item.findViewById<ImageView>(R.id.ivImage)
            if(ivImage.drawable != null) {
                val popup = LayoutInflater.from(item.context).inflate(R.layout.popup_dialog_place_item, null)
                val title = popup.findViewById<TextView>(R.id.tvPopupTitle)

                title.text = placeList[adapterPosition].title
                description.text = placeList[adapterPosition].description

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        description.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                    }
                }

                val imageView = popup.findViewById<ImageView>(R.id.ivPopupImage)
                imageView.setImageDrawable(ivImage.drawable)

                val dialog = Dialog(item.context)
                dialog.setContentView(popup)
                dialog.window?.setGravity(Gravity.CENTER)
                dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                dialog.show()

                popup.findViewById<Button>(R.id.btnBack).setOnClickListener {
                    dialog.dismiss()
                }
            }

            item.setOnTouchListener { _, _ ->
                if(position % 2 == 0) {
                    item.setCardBackgroundColor(Color.parseColor("#FFDFF8"))
                } else {
                    item.setCardBackgroundColor(Color.parseColor("#C1E2FF"))
                }
                false
            }
            return false
        }

        override fun onClick(p0: View?) {
            val intent = Intent(p0?.context, HappyPlaceInformationActivity::class.java)

            if(adapterPosition % 2 == 0) {
                intent.putExtra("color", "#FFDFF8")
            } else {
                intent.putExtra("color", "#C1E2FF")
            }

            intent.putExtra("id", placeList[adapterPosition].id)
            startActivity(p0!!.context!!, intent, null)
        }
    }
}


