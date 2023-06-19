package com.example.storyapp.view.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.adapter.ListStoryAdapter
import com.example.storyapp.adapter.LoadingStateAdapter
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.event.Event
import com.example.storyapp.preferences.UserPreference
import com.example.storyapp.response_model.LoginResult
import com.example.storyapp.view.add_story.AddStoryActivity
import com.example.storyapp.view.maps.MapsActivity
import com.example.storyapp.view.welcome.WelcomeActivity
import com.google.android.material.snackbar.Snackbar

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var user: LoginResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        if (intent != null) {
            IS_SUCCESS_LOGIN = intent.getBooleanExtra("IS_SUCCESS_LOGIN", false)
            IS_SUCCESS_UPLOAD = intent.getBooleanExtra("IS_SUCCESS_UPLOAD", false)
        }

        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.setHasFixedSize(true)

        setupView()
        setupViewModel()
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

        binding.rvStory.setHasFixedSize(true)
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[MainViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->
            if (user.token != "") {
                this.user = user
                getData()
                if (IS_SUCCESS_LOGIN) {
                    val successLogin = Event(getString(R.string.login_success))
                    successLogin.getContentIfNotHandled()?.let { snackbarText ->
                        mainViewModel.setSuccessLogin(snackbarText)
                    }
                }
                if (IS_SUCCESS_UPLOAD) {
                    val successUpload = Event(getString(R.string.upload_success))
                    successUpload.getContentIfNotHandled()?.let { snackbarText ->
                        mainViewModel.setSuccessUpload(snackbarText)
                    }
                }
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        mainViewModel.snackbarText.observe(this) {
            it.getContentIfNotHandled()?.let { snackbarText ->
                Snackbar
                    .make(
                        window.decorView.rootView,
                        snackbarText,
                        Snackbar.LENGTH_SHORT
                    )
                    .show()
            }
        }
        mainViewModel.isConnectionFailed.observe(this) {
            it.getContentIfNotHandled()?.let { isConnectionFailed ->
                showConnectionFailedToast(isConnectionFailed)
                binding.tvNoData.alpha = 1F
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.logout -> {
                mainViewModel.logout()
            }
            R.id.add_story -> {
                val moveToAddStory = Intent(this@MainActivity, AddStoryActivity::class.java)
                moveToAddStory.putExtra(AddStoryActivity.EXTRA_TOKEN, user.token)
                startActivity(moveToAddStory)
            }
            R.id.settings -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                IS_SUCCESS_UPLOAD = false
                IS_SUCCESS_LOGIN = false
            }
            R.id.change_view -> {
                val moveToMaps = Intent(this@MainActivity, MapsActivity::class.java)
                moveToMaps.putExtra(AddStoryActivity.EXTRA_TOKEN, user.token)
                startActivity(moveToMaps)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.tvNoData.alpha = 0F
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showConnectionFailedToast(isConnectionFailed: Boolean) {
        binding.tvNoData.alpha = 1F
        if (isConnectionFailed) {
            Toast.makeText(
                this,
                getString(R.string.connection_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getData() {
        showLoading(true)
        val adapter = ListStoryAdapter()
        binding.rvStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        adapter.addLoadStateListener { loadStates ->
            if (loadStates.refresh is LoadState.Error) {
                mainViewModel.setConnectionFailed(true)
            }
            showLoading(false)
        }
        mainViewModel.story.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    companion object {
        var IS_SUCCESS_LOGIN = false
        var IS_SUCCESS_UPLOAD = false
    }
}