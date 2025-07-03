package com.skyrist.bookhub

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import database.BookDatabase
import database.BookEntity

class ProfileFragment : Fragment() {

    private lateinit var txtEmail: TextView

    private lateinit var btnLogout: Button
    private lateinit var profilePic: ImageView
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var editAbout: EditText
    private lateinit var btnSaveAbout: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        txtEmail = view.findViewById(R.id.txtEmail)

        btnLogout = view.findViewById(R.id.btnLogout)
        profilePic = view.findViewById(R.id.profilePic)

        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        txtEmail.text = "Email: ${currentUser?.email ?: "Not logged in"}"

        editAbout = view.findViewById(R.id.editAbout)
        btnSaveAbout = view.findViewById(R.id.btnSaveAbout)

        sharedPreferences = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE)

// Load saved about
        editAbout.setText(sharedPreferences.getString("about", ""))

        btnSaveAbout.setOnClickListener {
            val aboutText = editAbout.text.toString()
            sharedPreferences.edit().putString("about", aboutText).apply()
            Toast.makeText(context, "About updated!", Toast.LENGTH_SHORT).show()
        }



        btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(requireContext(), SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }

        return view



    }





}
