package com.victoryil.wellder

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.victoryil.wellder.databinding.MainActivityBinding
import com.victoryil.wellder.instances.*
import com.victoryil.wellder.utils.debug
import com.victoryil.wellder.utils.goneUnless

class MainActivity : AppCompatActivity() {
    private val binding: MainActivityBinding by lazy {
        MainActivityBinding.inflate(layoutInflater)
    }
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController
    }

    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.mainFragmentDestination,
                R.id.sendOTPDestination
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setups()
    }

    private fun setups() {
        EmojiManager.install(GoogleEmojiProvider())
        requiredSingleton()
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = navController.currentDestination!!.label.toString()
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.title = destination.label
            binding.toolbar.goneUnless(!AppManager.checkHide())
            if (destination.id == R.id.settingsFragment) {
                NavigationHelper.setVisibilityOptionItem(false)
            }
            if (destination.id == R.id.chatFragmentDestination) {
                NavigationHelper.setVisibilityOptionItem(false)
            }
            if (destination.id == R.id.searchDestination) {
                NavigationHelper.setVisibilityOptionItem(false)
            }
        }
        checkUser()
    }

    private fun loadSingleton() {
        FirebaseHelper.loadFirebase()
        ServiceManager.loadServices(this)
    }

    private fun requiredSingleton() {
        NavigationHelper.setNavigationController(navController)
        Preferences.loadPreferences(this)
    }

    private fun checkUser() {
        if (FirebaseHelper.getCurrentUID() != null) {
            loadSingleton()
            FirebaseHelper.isReady.observe({ lifecycle }, {
                if (it) {
                    FirebaseHelper.userList.observe({ lifecycle }, {
                        AppManager.runApp(
                            FirebaseHelper.findUserByUID(FirebaseHelper.getCurrentUID()!!),
                            this
                        )
                    })
                    FirebaseHelper.profileChangesEmitter()
                }
            })
        } else NavigationHelper.navigateToSendOTP()
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onBackPressed() {
        if (AppManager.checkReturn()) finish()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        if (FirebaseHelper.getCurrentUID() != null) ServiceManager.getNotificationManager()
            .cancelAll()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!Preferences.getTheme()) {
            if (NavigationHelper.getCurrentDestination()?.id == R.id.mainFragmentDestination) {
                menuInflater.inflate(R.menu.toolbar, menu)
                NavigationHelper.setOptionItem(menu?.get(0))
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> NavigationHelper.navigateToSettings()
            R.id.ajustes -> NavigationHelper.navigateToSettings()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        if (MyUser.getMyUser() != null) {
            FirebaseHelper.setStatus(true)
        }
        super.onResume()
    }

    override fun onPause() {
        if (MyUser.getMyUser() != null) {
            FirebaseHelper.setStatus(false)
        }
        super.onPause()
    }
}
