package com.example.snapshots

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment() {

    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>
    private lateinit var mBinding: FragmentHomeBinding


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

        val options = FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query, Snapshot::class.java).build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot,SnapshotHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                TODO("Not yet implemented")
            }

            override fun onBindViewHolder(p0: SnapshotHolder, p1: Int, p2: Snapshot) {
                TODO("Not yet implemented")
            }

        }

    }

    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot){

        }
    }

}