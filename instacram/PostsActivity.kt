package com.example.instacram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instacram.models.Post
import com.example.instacram.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_posts.*

private const val TAG = "Posts Activity"
public const val EXTRA_USERNAME = "EXTRA_USERNAME"
open class PostsActivity : AppCompatActivity() {
    private lateinit var firestoredb: FirebaseFirestore
    private lateinit var posts:MutableList<Post>
    private lateinit var adapter: PostsAdapter
    private var signedInUser: User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        posts = mutableListOf()
        adapter = PostsAdapter(this, posts)

        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)
        firestoredb = FirebaseFirestore.getInstance()

        firestoredb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "Signed in ID $signedInUser")
            }

            .addOnFailureListener{ exception ->
                Log.i(TAG, "Access Failed", exception)
            }
        var postsReferance = firestoredb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        val username = intent.getStringExtra(EXTRA_USERNAME)

        if (username != null){
            supportActionBar?.title = username
            postsReferance = postsReferance.whereEqualTo("user.username", username)
        }
        postsReferance.addSnapshotListener{snapshot, exception ->
            if (exception != null || snapshot == null){
                Log.e(TAG, "exceptions detected", exception)
                return@addSnapshotListener
            }
            val postsList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postsList)
            adapter.notifyDataSetChanged()
            for (post in postsList){
                Log.i(TAG, "Post ${post}")
            }
        }

        fabCreate.setOnClickListener{
           val intent = Intent(this, CreateActivity::class.java )
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_profile){
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}