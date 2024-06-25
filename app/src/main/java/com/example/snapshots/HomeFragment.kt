package com.example.snapshots

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class HomeFragment : Fragment(), HomeAux{

    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>
    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mLayoutManager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = FirebaseDatabase.getInstance().reference.child("snapshots")

        //Se parsea los datos
        val options =
            FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query, SnapshotParser {
                val snapshot = it.getValue(Snapshot::class.java)
                snapshot!!.id = it.key!!
                snapshot
            } ).build()
            //FirebaseRecyclerOptions.Builder<Snapshot>()
            //.setQuery(query, Snapshot::class.java).build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot,SnapshotHolder>(options){
            private lateinit var mContext: Context
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                mContext = parent.context

                val view = LayoutInflater.from(mContext).inflate(R.layout.item_snapshot, parent, false)
                return SnapshotHolder(view)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
                val snapshot = getItem(position)

                with(holder){
                    setListener(snapshot)
                    binding.tvTitle.text = snapshot.title
                    binding.cbLike.text = snapshot.likeList.keys.size.toString()
                    FirebaseAuth.getInstance().currentUser?.let {
                        binding.cbLike.isChecked = snapshot.likeList.containsKey(it.uid)
                    }

                    Glide.with(mContext)
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgPhoto)

                    binding.btnDelete.visibility= if (model.ownerUid == FirebaseAuth.getInstance().currentUser!!.uid){
                        View.VISIBLE
                    }else{
                        View.INVISIBLE
                    }

                }
            }

            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.progressBar.visibility = View.GONE
                notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(mContext, error.message, Toast.LENGTH_LONG).show()
            }

        }

        mLayoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }

    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }

    private fun deleteSnapshot(snapshot: Snapshot){
        context?.let {
            MaterialAlertDialogBuilder(
                it
            )
                .setTitle(R.string.dialog_delete_title)
                .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                    Log.d("TEST", snapshot.id)
                    val storageSnapshotRef =
                        FirebaseStorage.getInstance().reference.child("snapshots")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(snapshot.id)
                    storageSnapshotRef.delete().addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            val databaseReference =
                                FirebaseDatabase.getInstance().reference.child("snapshots")
                            databaseReference.child(snapshot.id).removeValue()
                        } else {
                            Snackbar.make(mBinding.root, "Error al eliminar", Snackbar.LENGTH_LONG)
                                .show()
                        }
                    }
                    // Do something
                }
                .setNegativeButton(R.string.dialog_delete_cancel, null)
                .show()
        }


    }

    private fun setLike(snapshot: Snapshot, checked: Boolean){
        val databaseReference = FirebaseDatabase.getInstance().reference.child("snapshots")
        if(checked){
            databaseReference.child(snapshot.id).child("likeList")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(checked)
        }else{
            databaseReference.child(snapshot.id).child("likeList")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(null)
        }
    }
    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot){
            binding.btnDelete.setOnClickListener {
                deleteSnapshot(snapshot)
            }
            binding.cbLike.setOnCheckedChangeListener { buttonView, checked ->
                setLike(snapshot, checked)
            }
        }
    }

    override fun goToTop() {
        mBinding.recyclerView.smoothScrollToPosition(0)
    }

}