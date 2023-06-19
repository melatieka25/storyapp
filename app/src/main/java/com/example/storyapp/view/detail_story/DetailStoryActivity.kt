package com.example.storyapp.view.detail_story

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivityDetailStoryBinding
import com.example.storyapp.helper.Helper.withDateFormat
import com.example.storyapp.response_model.ListStoryItem
import java.util.*

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding
    private lateinit var story: ListStoryItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        if (intent != null) {
            story = intent.getParcelableExtra(EXTRA_STORY)!!
        }

        setupView()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        with(binding) {
            tvDetailName.text = story.name
            tvDetailCreatedAt.text = story.createdAt?.withDateFormat(Locale.getDefault())
            tvDetailDescription.text = story.description
            if (story.lat != null && story.lon != null) {
                tvDetailLocation.text = getString(R.string.location_detail, story.lat, story.lon)
            } else {
                tvDetailLocation.text = getString(R.string.location_not_found)
            }

            Glide.with(this@DetailStoryActivity).load(story.photoUrl).into(ivDetailPhoto)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        var EXTRA_STORY = "extra_story"
    }
}