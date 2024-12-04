package edu.psu.sweng888.wanderverseapp

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseManager {
    private val firestore = FirebaseFirestore.getInstance()
    private var collection = "test"
    private var document = "Yx8fIx3Ckl00UmnhE6Vb"

    // Function to get the document reference based on current collection and document values
    private fun getReference(): DocumentReference {
        return firestore.collection(this.collection).document(this.document)
    }

    // Update the collection name
    fun setCollection(collection: String) {
        this.collection = collection
    }

    // Update the document name
    fun setDocument(document: String) {
        this.document = document
    }

    // Read documents in a collection
    fun readDocuments(onDocumentsReceived: (List<DocumentReference>) -> Unit) {
        firestore.collection(this.collection).get().addOnSuccessListener { result ->
            val documents = mutableListOf<DocumentReference>()
            for (document in result) {
                documents.add(document.reference)
            }
            onDocumentsReceived(documents)
        }
    }

    // Write a field to the document
    fun writeField(key: String, value: String, onComplete: (Boolean) -> Unit) {
        val data = hashMapOf(key to value)
        getReference().set(data).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun updateField(key: String, value: Any, onComplete: (Boolean) -> Unit) {
        getReference().update(key, value)
            .addOnSuccessListener {
                Log.d("FirebaseManager", "Field $key successfully updated to $value")
                onComplete(true) // Notify success
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseManager", "Failed to update field $key to $value", e)
                onComplete(false) // Notify failure
            }
    }

    // Read one value from the document based on the key
    fun readValue(field: String, onDataReceived: (String?) -> Unit) {
        getReference().get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val data = document.getString(field)
                onDataReceived(data)
            } else {
                onDataReceived(null)
            }
        }.addOnFailureListener {
            onDataReceived(null)
        }
    }

    // Read all fields from the document
    fun readAllFields(onDataReceived: (Map<String, Any>?) -> Unit) {
        getReference().get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val data = document.data // Retrieves all fields as a map
                onDataReceived(data)
            } else {
                onDataReceived(null)
            }
        }.addOnFailureListener {
            onDataReceived(null)
        }
    }

    // Create new document
    fun createNewDocument(data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        firestore.collection(this.collection).add(data).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

//    This function is used to update the distance traveled in the database for a particular user.
fun updateProgress(userId: String, rewardId: String, distance: Double, isIncrement: Boolean = true) {
    // Query the collection to find the document with the matching userId and rewardId
    firestore.collection(collection)
        .whereEqualTo("userId", userId)
        .whereEqualTo("rewardId", rewardId)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (querySnapshot != null && !querySnapshot.isEmpty) {
                // Get the reference of the matching document
                val documentRef = querySnapshot.documents[0].reference

                if (isIncrement) {
                    // Increment the progress field
                    documentRef.update("progress", FieldValue.increment(distance))
                        .addOnSuccessListener {
                            Log.d("FirebaseUpdate", "Progress for user $userId and reward $rewardId incremented by $distance successfully.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseUpdate", "Error incrementing progress: $e")
                        }
                } else {
                    // Overwrite the progress field
                    documentRef.update("progress", distance)
                        .addOnSuccessListener {
                            Log.d("FirebaseUpdate", "Progress for user $userId and reward $rewardId set to $distance successfully.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseUpdate", "Error setting progress: $e")
                        }
                }
            } else {
                Log.e("FirebaseUpdate", "No matching document found for user $userId and reward $rewardId")
            }
        }
        .addOnFailureListener { e ->
            Log.e("FirebaseUpdate", "Error querying Firestore: $e")
        }
}



}
