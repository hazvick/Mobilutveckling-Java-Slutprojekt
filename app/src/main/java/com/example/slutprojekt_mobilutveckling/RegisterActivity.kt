package com.example.slutprojekt_mobilutveckling

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        edit_button_registration.setOnClickListener {
            performRegister()
        }

        edit_already_have_account.setOnClickListener {
            //launch loginpage
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectphoto_button_register.setOnClickListener {
            Log.d("RegisterActivity", "Selecting photo.")

            //fun for picking images from library
            val intent = Intent(Intent.ACTION_PICK)                                                 //lets user ACTION.PICK, with intent.type image, then sends requestCode 0
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    //selected photo to be used outside of onActivityResult
    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {                 //catches if requestCode is 0, result is OK and data is not null
            //proceed and check selected image
            Log.d("RegisterActivity", "Image was successfully selected.")

            //represents the location where image is stored on device
            selectedPhotoUri = data.data

            //getting access to the bitmap of the selected photo
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)

            selectphoto_button_register.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)
            Toast.makeText(this, "Profile image has been set!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performRegister() {
        //registration input variables
        val username = edit_username_registration.text.toString()
        val email = edit_email_registration.text.toString()
        val password = edit_password_registration.text.toString()

        //preventing crash if inputfields are left empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Invalid input, please try again.", Toast.LENGTH_LONG).show()
            return
        }

        //Firebase authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                //checks if creation was unsuccessful
                if (!it.isSuccessful) return@addOnCompleteListener

                //else if successful
                Log.d("RegisterActivity", "Successfully created user with uid: ${it.result?.user?.uid}")
                Toast.makeText(this, "Account has been created.", Toast.LENGTH_LONG).show()

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Account could not be created: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {

        if (selectedPhotoUri == null) return //first if-statement, returns if value is null

        //random UUID
        val filename = UUID.randomUUID().toString()

        //create directory in Firebase storage to save images
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!) //the double (!!) throws a NullPointerException if value is null, but it wont be because of first if-statement
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully added image to Firebase Storage: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File location: $it")

                    saveUserToFirebaseDatabase(it.toString()) //sends "it" to fun saveUserToFirebaseDatabase()
                }
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Could not add image to Firebase Storage")
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageURL: String) {
        val uid = FirebaseAuth.getInstance().uid ?: "" //if null, sets empty string
        val ref = FirebaseDatabase.getInstance("https://slutprojektmobilutveckling-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/$uid")

        val user = User(uid, edit_username_registration.text.toString(), profileImageURL)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Saved user to Firebase Database.")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                //flags clears previous activity, so if user register account user will be redirected to LatestMessagesActivity
                //if user tries to go back using Android Navigation, he will not go back to Registration but to Home Screen
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Could not save user to Firebase Database")
            }
    }
}

@Parcelize //possible by setting android to experimental = true in build.gradle:app
class User(val uid: String, val username: String, val profileImageURL: String): Parcelable { //Parcelable makes it possible to pass entire objects using intent.putExtra
    constructor() : this("", "", "") //was getting no-argument constructor error, this resolves it
}