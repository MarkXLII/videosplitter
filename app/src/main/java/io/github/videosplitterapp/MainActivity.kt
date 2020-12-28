package io.github.videosplitterapp

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.databinding.MainActivityBinding
import io.github.videosplitterapp.splitsManager.SplitsManager
import io.github.videosplitterapp.splitsManager.SplitsManager.State.*
import io.github.videosplitterapp.splitsManager.SplitsManagerImpl


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val splitsManager: SplitsManagerImpl by viewModels()
    private lateinit var navController: NavController

    companion object {
        private val TAG = MainActivity::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)
            .also {
                it.splitsManager = splitsManager
                it.lifecycleOwner = this
                it.executePendingBindings()
                val host: NavHostFragment =
                    supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment?
                        ?: return
                navController = host.navController
                it.bottomNavigation.setupWithNavController(navController)
                it.topAppBar.setupWithNavController(
                    navController,
                    AppBarConfiguration(navController.graph)
                )
                setSupportActionBar(it.topAppBar)
                NavigationUI.setupActionBarWithNavController(this, navController)
            }
        splitsManager.migrateStorageToPublicDir()
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "onSupportNavigateUp() called")
        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment)
        val fragment = navHost?.childFragmentManager?.primaryNavigationFragment
        return if (fragment is BaseTopFragment) {
            fragment.backPressed()
            true
        } else {
            navController.navigateUp() || super.onSupportNavigateUp()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                navController.navigate(R.id.action_global_settingsActivity)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

@BindingAdapter(value = ["state"])
fun BottomNavigationView.setState(state: SplitsManager.State?) {
    visibility = when (state) {
        IDLE -> View.GONE
        PROCESSING_VIDEO -> View.GONE
        READY_TO_SPLIT -> View.VISIBLE
        SPLITTING -> View.VISIBLE
        SPLITTING_DONE -> View.VISIBLE
        SPLITTING_ERROR -> View.VISIBLE
        else -> View.GONE
    }
}