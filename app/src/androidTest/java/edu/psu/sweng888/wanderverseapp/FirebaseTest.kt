package edu.psu.sweng888.wanderverseapp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseTest {

    @Test
    fun connectToFirebase() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(appContext)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference = database.getReference("Yx8fIx3Ckl00UmnhE6Vb")

        // Using CountDownLatch to wait for Firebase response
        val latch = CountDownLatch(1)

        reference.setValue("Test Message").addOnCompleteListener { task ->
            assertTrue("Failed to connect to Firebase", task.isSuccessful)
            latch.countDown() // Release the latch when task is complete
        }

        // Wait for a maximum of 10 seconds for the task to complete
        latch.await(10, TimeUnit.SECONDS)
    }
}