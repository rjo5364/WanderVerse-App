package edu.psu.sweng888.wanderverseapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseTest {

    @Test
    fun connectToFirebase() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(appContext)

        val firestore = FirebaseFirestore.getInstance()
        val reference = firestore.collection("test").document("Yx8fIx3Ckl00UmnhE6Vb")

        // Using CountDownLatch to wait for Firebase response
        val latch = CountDownLatch(1)

        val data = hashMapOf("Value" to "Test Message")
        reference.set(data).addOnCompleteListener { task ->
            assertTrue("Failed to connect to Firebase", task.isSuccessful)
            latch.countDown() // Release the latch when task is complete
        }

        // Wait for a maximum of 10 seconds for the task to complete
        latch.await(10, TimeUnit.SECONDS)
    }

    @Test
    fun readFromFirebase() {
        val firebase = FirebaseManager()

        firebase.readAllFields { data ->
            println(data)

            assertNotNull(data)
        }
    }

}
