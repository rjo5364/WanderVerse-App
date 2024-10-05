package edu.psu.sweng888.wanderverseapp

import com.google.firebase.firestore.FirebaseFirestore

class FirebaseManager {
    private val firestore = FirebaseFirestore.getInstance()
    private var collection = "test"
    private var document = "Yx8fIx3Ckl00UmnhE6Vb"
    private val reference = firestore.collection(this.collection).document(this.document)


    fun set_collection(collection: String){
        this.collection = collection;
    }

    fun set_document(document: String){
        this.document = document;
    }

//
    private fun get_refrence() {
        firestore.collection(this.collection).document(this.document)
    }

    // Write A field to the document
    fun writeField(key: String, value: String, onComplete: (Boolean) -> Unit) {
        val data = hashMapOf(key to value)
        reference.set(data).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

//    Read one value from the document based on the key
    fun readValue(field: String, onDataReceived: (String?) -> Unit) {
        reference.get().addOnSuccessListener { document ->
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
        reference.get().addOnSuccessListener { document ->
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
}
