package edu.psu.sweng888.wanderverseapp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import com.google.firebase.FirebaseApp

@RunWith(AndroidJUnit4::class)
class FirebaseTest {

    @Test
    fun connectToFirebase() {
        // Initialize Firebase in the test context
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(appContext)

        // Get reference to Firebase Database
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.getReference("test_message")

        // Write to the database
        reference.setValue("Test Message").addOnCompleteListener { task ->
            assertTrue("Failed to connect to Firebase", task.isSuccessful)
        }
    }
}