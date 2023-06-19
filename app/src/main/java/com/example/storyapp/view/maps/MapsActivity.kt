package com.example.storyapp.view.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.databinding.ActivityMapsBinding
import com.example.storyapp.preferences.UserPreference
import com.example.storyapp.response_model.ListStoryItem
import com.example.storyapp.view.add_story.AddStoryActivity
import com.example.storyapp.view.detail_story.DetailStoryActivity
import com.example.storyapp.view.main.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        token = intent.getStringExtra(AddStoryActivity.EXTRA_TOKEN)

        setupViewModel()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        getMyLocation()
        setMapStyle()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.settings -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.add_story -> {
                val moveToAddStory = Intent(this@MapsActivity, AddStoryActivity::class.java)
                moveToAddStory.putExtra(AddStoryActivity.EXTRA_TOKEN, token)
                startActivity(moveToAddStory)
                true
            }
            R.id.change_view -> {
                val moveToMain = Intent(this@MapsActivity, MainActivity::class.java)
                moveToMain.putExtra(AddStoryActivity.EXTRA_TOKEN, token)
                startActivity(moveToMain)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun setupViewModel() {
        mapsViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[MapsViewModel::class.java]

        token?.let { mapsViewModel.getStoriesWithLocation(it) }

        mapsViewModel.snackbarText.observe(this) {
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

        mapsViewModel.listStory.observe(this) { stories ->
            setStoriesMap(stories)
        }

        mapsViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        mapsViewModel.isConnectionFailed.observe(this) {
            it.getContentIfNotHandled()?.let { isConnectionFailed ->
                showConnectionFailedToast(isConnectionFailed)
                binding.tvNoData.alpha = 1F
            }
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
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

    private val boundsBuilder = LatLngBounds.Builder()

    private fun setStoriesMap(listStory: List<ListStoryItem>) {
        if (listStory.isEmpty()) {
            binding.tvNoData.alpha = 1F
        } else {
            binding.tvNoData.alpha = 0F
            listStory.forEach { story ->
                val latLng = story.lat?.let { story.lon?.let { it1 -> LatLng(it, it1) } }
                val marker = latLng?.let {
                    MarkerOptions().position(it).title(story.name).snippet(story.description)
                }
                    ?.let { mMap.addMarker(it) }
                if (marker != null) {
                    marker.tag = story
                }

                if (latLng != null) {
                    boundsBuilder.include(latLng)
                }
            }
            val bounds: LatLngBounds = boundsBuilder.build()

            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50)
            mMap.animateCamera(cameraUpdate)
        }

        mMap.setOnInfoWindowClickListener { marker ->
            val story = marker.tag as ListStoryItem
            val moveToDetailUser = Intent(this@MapsActivity, DetailStoryActivity::class.java)
            moveToDetailUser.putExtra(DetailStoryActivity.EXTRA_STORY, story)
            startActivity(moveToDetailUser)
        }
    }

    companion object {
        const val TAG = "Maps Activity"
    }
}