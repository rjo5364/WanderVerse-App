package edu.psu.sweng888.wanderverseapp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FireBaseManager {
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var reference: DatabaseReference = database.getReference("messages")

    fun writeMessage(message: String, onComplete: (Boolean) -> Unit) {
        reference.setValue(message).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun readMessage(onDataReceived: (String?) -> Unit) {
        reference.get().addOnSuccessListener { snapshot ->
            val message = snapshot.getValue(String::class.java)
            onDataReceived(message)
        }.addOnFailureListener {
            onDataReceived(null)
        }
    }
}