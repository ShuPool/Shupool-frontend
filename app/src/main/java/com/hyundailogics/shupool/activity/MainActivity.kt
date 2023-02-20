package com.hyundailogics.shupool.activity

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.application.GlobalApplication
import com.hyundailogics.shupool.databinding.ActivityMainBinding
import com.hyundailogics.shupool.fragment.*
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.common.objects.KNSearchPOI
import com.sothree.slidinguppanel.SlidingUpPanelLayout

const val fragmentMoveMapTag = "Move"
const val fragmentMapTag = "Map"
const val fragmentSearchTag = "Search"
const val fragmentDriverRouteTag = "Drive"

class MainActivity : BaseActivity(), FragmentSearchListener {
    private lateinit var mMenu: Menu
    private lateinit var binding: ActivityMainBinding
    private var mode = DestinationSearch
    var imm : InputMethodManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.initialize()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        initToolBar()
        initNavigationView()
        initFragments()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mode = intent?.getIntExtra("mode", DestinationSearch) ?: DestinationSearch
        if (mode == WayPointSearch) {
            mMenu.performIdentifierAction(R.id.search_start, 0)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        menu?.findItem(R.id.search_start)?.let {
            (it.actionView as SearchView).apply {
                setSearchableInfo((getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(componentName))
                isSubmitButtonEnabled = true

                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        (supportFragmentManager.findFragmentByTag(fragmentDriverRouteTag) as FragmentDriverRoute).reqSearch(query!!)
                        return false
                    }
                })
            }

            it.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                val moveMarker = menu.findItem(R.id.move_map)
                override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                    moveMarker.isVisible=true
                    addFragment(binding.frameMainLayout.id, FragmentSearch(mode == DestinationSearch), fragmentSearchTag)
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {

                    if (mode != WayPointSearch) {
                        moveMarker.isVisible=false
                        removeFragment(supportFragmentManager.fragments.last())
                    } else {
                        finish()
                    }
                    return true
                }
            })
            mMenu = menu
        }

        if (mode == WayPointSearch) {
            mMenu.performIdentifierAction(R.id.start_search, 0)
        }

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            R.id.search_start -> {
                return true
            }
            R.id.move_map -> {
                hideKeyborad(binding.frameMainLayout)
                addFragment(binding.frameMainLayout.id, FragmentFindLocationMarker(), fragmentMoveMapTag)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun initToolBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initNavigationView() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawers()

            when (menuItem.itemId) {
                R.id.main_drawer_item_search -> {
                    mMenu.performIdentifierAction(R.id.start_search, 0)
                }

                R.id.main_drawer_item_dguide -> {
                    val intent = Intent(this, KNNaviActivity::class.java)
                    startActivity(intent)
                }
                R.id.main_drawer_driver_test -> {
                    addFragment(binding.frameMainLayout.id, FragmentDriverRoute(), "Test")
                }
            }
            true
        }
    }

    private fun initFragments() {
        addFragment(binding.frameMainLayout.id, FragmentMap(), fragmentMapTag)
    }

    override fun onSearchResult(poi: KNSearchPOI, avoidOption: Int, routeOption: KNRoutePriority?) {
        if (mode == DestinationSearch) {
            viewFullRoute(poi, avoidOption, routeOption)

        } else {
            val via = poi.guidePoints?.get(0) ?: KNPOI(poi.name, poi.pos.x, poi.pos.y, poi.address)
            val intent : Intent = intent.putExtra("wayPoint", via)
            setResult(RESULT_OK, intent)

            mMenu.findItem(R.id.start_search)?.collapseActionView()
        }
    }
    private fun viewFullRoute(poi: KNSearchPOI, avoidOption: Int, routeOption: KNRoutePriority?) {
        val goal = poi.guidePoints?.get(0)?.pos ?: KNPOI(poi.name, poi.pos.x, poi.pos.y, poi.address)
        val intent = Intent(this, FullRouteActivity::class.java)
        intent.putExtra("goal", goal)
        intent.putExtra("avoidOption", avoidOption)
        intent.putExtra("routeOption", routeOption)
        initForResult.launch(intent)
    }

    private val initForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when(result.resultCode) {
                Activity.RESULT_OK -> {
                    mMenu.findItem(R.id.start_search)?.collapseActionView()
                }

                Activity.RESULT_CANCELED -> {
                }
            }
        }

    override fun onBackPressed() {
        removeFragment(supportFragmentManager.fragments.last())
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalApplication.knsdk.sharedGuidance()?.stop()
    }

    //키보드 숨기기
    fun hideKeyborad(v: View) {
        if(v != null) {
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}