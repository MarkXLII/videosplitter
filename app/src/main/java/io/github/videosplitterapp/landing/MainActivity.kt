package io.github.videosplitterapp.landing

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.BaseTopFragment
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.MainActivityBinding


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { MainActivityBinding.inflate(layoutInflater) }
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment)
            .navController
    }
    private val migrationAlertDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.please_wait))
            .setMessage(getString(R.string.migrating_videos))
            .setCancelable(false)
            .create()
    }
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initNavController()
        initListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
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

    private fun initView() {
        setContentView(binding.root)
    }

    private fun initNavController() {
        binding.bottomNavigation.setupWithNavController(navController = navController)
        binding.topAppBar.setupWithNavController(
            navController = navController,
            configuration = AppBarConfiguration(navController.graph)
        )
        setSupportActionBar(binding.topAppBar)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    private fun initListeners() {
        viewModel.showMigrationAlertDialog.observe(this, Observer {
            val show = it ?: return@Observer
            migrationAlertDialog.run { if (show) show() else dismiss() }
        })
        viewModel.showBottomNav.observe(this, Observer {
            val show = it ?: return@Observer
            binding.bottomNavigation.visibility = if (show) View.VISIBLE else View.GONE
        })
    }
}
