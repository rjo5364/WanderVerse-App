package edu.psu.sweng888.wanderverseapp

import com.google.firebase.firestore.DocumentReference
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

}
