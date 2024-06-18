package com.example.snapshots

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.snapshots.databinding.FragmentAddBinding
import com.example.snapshots.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddFragment : Fragment() {

    private val RC_GALLERY = 18
    private val PATH_SNAPSHOT  = "snapshots"
    private lateinit var mBinding: FragmentAddBinding

    private var mPhotoSelectedUrl: Uri? = null
    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.btnPost.setOnClickListener {
            postSnapshot()
        }
        mBinding.btnSelect.setOnClickListener {
            openGallery()
        }
        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GALLERY)
    }

    private fun postSnapshot() {
        mBinding.progressBar.visibility = View.VISIBLE
        val key = mDatabaseReference.push().key!!
        val storageRef = mStorageReference.child(PATH_SNAPSHOT).child("my_photo")
        if(mPhotoSelectedUrl != null){
            storageRef.putFile(mPhotoSelectedUrl!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred/it.totalByteCount).toDouble()
                    mBinding.progressBar.progress = progress.toInt()
                    mBinding.tvMessage.text = "$progress%"
                }
                .addOnCompleteListener{
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    Snackbar.make(mBinding.root, "Instantánea publicada", Snackbar.LENGTH_SHORT).show()
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveSnapshot(key, it.toString(), mBinding.etTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                    }
                }
                .addOnFailureListener{
                    Snackbar.make(mBinding.root, "No se pudo subir", Snackbar.LENGTH_SHORT).show()
                }
        }

    }

    private fun saveSnapshot(key: String, url: String, title: String){
        val snapshot = Snapshot(id = key, title = title, photoUrl = url)
        mDatabaseReference.child(key).setValue(snapshot)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == RC_GALLERY){
                mPhotoSelectedUrl = data?.data
                mBinding.imgPhoto.setImageURI(mPhotoSelectedUrl)
                mBinding.tilTitle.visibility = View.VISIBLE
                mBinding.tvMessage.text = getString(R.string.post_message_valid_title)
            }
        }
    }

}