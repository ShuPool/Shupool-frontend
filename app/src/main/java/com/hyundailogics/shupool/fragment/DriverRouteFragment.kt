package com.hyundailogics.shupool.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.children
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.application.GlobalApplication
import com.hyundailogics.shupool.databinding.FragmentDriverRouteBinding
import com.kakaomobility.knsample.adapter.FragmentSearchAdapter
import com.kakaomobility.knsample.adapter.FragmentSearchAdapterListener
import com.kakaomobility.knsdk.*
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_X
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_Y
import com.kakaomobility.knsdk.common.objects.KNSearchPOI
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.common.util.IntPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.sothree.slidinguppanel.SlidingUpPanelLayout


class DriverRouteFragment(searchGoal: Boolean = true) : FragmentBaseMap() {
    private var _binding: FragmentDriverRouteBinding? = null
    private val binding get() = _binding!!

    lateinit var slidePanel : SlidingUpPanelLayout

    private lateinit var searchPos: IntPoint
    private lateinit var searchKeyword: String
    private var lastPageIdxOfAddress = 1
    private var lastPageIdxOfPlace = 1

    private var isSearchGoal = searchGoal

    private var mListener: FragmentSearchListener? = null

    val View.hasFocusRec: Boolean get() = when {
        isFocused -> true
        this is ViewGroup -> children.any { it.hasFocusRec }
        else -> false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        GlobalApplication.knsdk.sharedGpsManager()!!.recentGpsData.apply {
            searchPos = IntPoint(pos.x.toInt(), pos.y.toInt())
        }
        val historyString = getSearchKeywordHistory()?.split(",")
        _binding = FragmentDriverRouteBinding.inflate(inflater, container, false)

        initMapView(binding.mapView)

        var searchViewTextListener: SearchView.OnQueryTextListener =
            object : SearchView.OnQueryTextListener {
                //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
                override fun onQueryTextSubmit(s: String): Boolean {
                    return false
                }

                //텍스트 입력/수정시에 호출
                override fun onQueryTextChange(s: String): Boolean {
                    reqSearch(s!!)
                    return false
                }
            }

        binding.startSearch.setOnQueryTextListener(searchViewTextListener)
        binding.destinationSearch.setOnQueryTextListener(searchViewTextListener)

        return binding.root.apply {
            binding.recyclerView.adapter = FragmentSearchAdapter(object : FragmentSearchAdapterListener {
                // 출발 버튼 클릭 시
                override fun onItemClick(poi: KNSearchPOI) {
                    if(binding.startSearch.hasFocusRec) {
                        binding.startSearch.setQuery(poi.name, false)
                        binding.startSearch.clearFocus()
                    }
                    else if(binding.destinationSearch.hasFocusRec) {
                        binding.destinationSearch.setQuery(poi.name, false)
                        binding.destinationSearch.clearFocus()
                    }

                    mListener?.onSearchResult(poi)
                }

                override fun onSearchMore(type: KNSearchReqType) {
                    val lastIdx = when(type.value) {
                        "address" -> { ++lastPageIdxOfAddress }
                        "place" -> { ++lastPageIdxOfPlace }
                        else -> { 1 }
                    }

                    reqSearchWithType(type, lastIdx)
                }
            }, isSearchGoal)
            binding.recyclerView.visibility = if (historyString?.isNullOrEmpty() == false) View.GONE else View.VISIBLE
            binding.recyclerView2.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

                inner class Holder(view: View): RecyclerView.ViewHolder(view) {
                    val searchKeyword: TextView = view.findViewById(R.id.poi_name)
                    init {
                        // 최근기록 (출발 버튼 X)
                        view.setOnClickListener {
                            Log.d("최근기록", searchKeyword.text.toString())

                            if(binding.startSearch.hasFocusRec) {
                                binding.startSearch.setQuery(searchKeyword.text.toString(), false)
                            }
                            else if(binding.destinationSearch.hasFocusRec) {
                                binding.destinationSearch.setQuery(searchKeyword.text.toString(), false)
                            }

                            val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow((context as Activity).currentFocus?.windowToken, 0)

                            reqSearch(searchKeyword.text.toString())
                        }
                    }
                    fun setKeyword(keyword: String) {
                        searchKeyword.text = keyword
                    }
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    return Holder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.fragment_search_history_item, parent, false))
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    historyString?.let {
                        (holder as Holder).setKeyword(it[position])
                    }
                }

                override fun getItemCount(): Int {
                    return historyString?.size ?: 0
                }
            }
            binding.recyclerView2.visibility = if (historyString?.isNullOrEmpty() == false) View.VISIBLE else View.GONE

            binding.recyclerView2.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            binding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        }
    }
    private fun initMapView(mapView: KNMapView) {
        KNSDK.makeMapViewWithFrame(mapView) { error ->
            if (error != null) {
                Toast.makeText(context, "맵 초기화 작업이 실패하였습니다. \n[${error.code}] : ${error.msg}",
                    Toast.LENGTH_LONG).show()
                return@makeMapViewWithFrame
            }


            val lastPos = KNSDK.sharedGpsManager()?.lastValidGpsData?.pos ?: FloatPoint(
                KN_DEFAULT_POS_X.toFloat(), KN_DEFAULT_POS_Y.toFloat())
            mapView.moveCamera(KNMapCameraUpdate.targetTo(lastPos).zoomTo(2.5f), false)
        }
    }

    private fun swapList(isHistoryShow: Boolean) {
        if (isHistoryShow) {
            binding.recyclerView.visibility = View.GONE
            binding.recyclerView2.visibility = View.VISIBLE
        } else {
            binding.recyclerView2.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    fun reqSearch(aKeyword: String) {
        searchKeyword = aKeyword
        lastPageIdxOfAddress = 1
        lastPageIdxOfPlace = 1

        swapList(false)
        Handler(Looper.getMainLooper()).post {
            (binding.recyclerView.adapter as FragmentSearchAdapter).clear()
        }
        //검색 히스토리 저장.
        storeSearchKeywordHistory(aKeyword)
        reqSearchWithType(KNSearchReqType.KNSearchReqType_1, lastPageIdxOfAddress)
    }

    private fun reqSearchWithType(aRequest: KNSearchReqType, aLastIdx: Int) {
        GlobalApplication.knsdk.reqSearch(searchKeyword, aLastIdx, SEARCH_POI_COUNT_PER_PAGE, aRequest, searchPos.x, searchPos.y) { aError, aResult ->
            if (aError != null) {
                showTextError("${aError.code} ${aError.msg}")
            } else {
                Handler(Looper.getMainLooper()).post {
                    (binding.recyclerView.adapter as FragmentSearchAdapter).apply {
                        aResult?.let {
                            it.addressResult?.let { result ->
                                setAddressesData(result)
                            }

                            it.placeResult?.let { result ->
                                setPlacesData(result)
                            }
                        }

                        if (hasSearchResult()) {
                            showRecyclerView()
                        } else {
                            showTextError("검색 결과가 없습니다.")
                        }
                    }
                }
            }
        }
    }

    private fun showTextError(msg: String) {
        binding.txtError.text = msg
        binding.txtError.visibility = View.VISIBLE

        binding.recyclerView.visibility = View.GONE
    }

    private fun showRecyclerView() {
        binding.recyclerView.invalidateItemDecorations()
        binding.recyclerView.visibility = View.VISIBLE

        binding.txtError.visibility = View.GONE
    }
}