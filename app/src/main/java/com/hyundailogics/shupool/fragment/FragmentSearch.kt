package com.hyundailogics.shupool.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.application.GlobalApplication
import com.hyundailogics.shupool.carTypeWithIdx
import com.hyundailogics.shupool.databinding.FragmentSearchBinding
import com.kakaomobility.knsample.adapter.*
import com.kakaomobility.knsdk.*
import com.kakaomobility.knsdk.common.objects.KNSearchPOI
import com.kakaomobility.knsdk.common.util.IntPoint
import java.lang.RuntimeException

const val SEARCH_POI_COUNT_PER_PAGE = 15

interface FragmentSearchListener {
    fun onSearchResult(poi: KNSearchPOI, avoidOption: Int = 0, routeOption: KNRoutePriority? = null)
}

class FragmentSearch(searchGoal: Boolean = true) : Fragment() {

    data class optionListItem (
        val viewType: Int,
        val displayName: String,
        val avoidOption: KNRouteAvoidOption? = null,
        val routeOption: KNRoutePriority? = null
    )

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchPos: IntPoint
    private lateinit var searchKeyword: String
    private var lastPageIdxOfAddress = 1
    private var lastPageIdxOfPlace = 1

    private var isSearchGoal = searchGoal

    private var mListener: FragmentSearchListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is FragmentSearchListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString())
        }
    }

    override fun onDetach() {
        super.onDetach()

        mListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        GlobalApplication.knsdk.sharedGpsManager()!!.recentGpsData.apply {
            searchPos = IntPoint(pos.x.toInt(), pos.y.toInt())
        }
        val historyString = getSearchKeywordHistory()?.split(",")
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding.root.apply {
            binding.recyclerView.adapter = FragmentSearchAdapter(object : FragmentSearchAdapterListener {
                override fun onItemClick(poi: KNSearchPOI) {
                    //도착 눌렀을때
//                    mListener?.onSearchResult(poi) //길안내로 바로 넘어감
                    if (isSearchGoal) {
                        binding.routeOptionList.visibility = View.VISIBLE
                        (binding.routeOptionList.adapter as routeOptionAdapter).setSearchPoi(poi)

                    } else {
                        mListener?.onSearchResult(poi)
                    }
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
                    val searchKeyword: TextView = view.findViewById(R.id.poi_address)
                    init {
                        view.setOnClickListener {

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

            if (isSearchGoal) {
                makeRouteOption(this)
            }
        }
    }

    private fun makeRouteOption(view: View) {
        with(view) {
            binding.routeOptionList.setHasFixedSize(true)
            binding.routeOptionList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            binding.routeOptionList.adapter = routeOptionAdapter(mListener)
            binding.routeOptionList.visibility = View.GONE
        }
    }

    private class routeOptionAdapter(listener: FragmentSearchListener?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class categoryHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val header_name: TextView

            init {
                header_name = itemView.findViewById(R.id.header_name)
            }
        }

        inner class routeHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val layout: RelativeLayout
            val poi_name: TextView
            val btnSelect: Button
            val check: CheckBox

            init {
                layout= itemView.findViewById(R.id.btnLayout)
                poi_name = itemView.findViewById(R.id.poi_name)
                btnSelect = itemView.findViewById(R.id.btnSelect)
                check = itemView.findViewById(R.id.btnCheck)
            }
        }

        private var optionListener = listener

        private var list_Header = 0
        private var list_avoid = 1
        private var list_route = 2

        private var viewList : MutableList<optionListItem>
        private var avoidOptionValue = 0

        private fun setSectionList(): MutableList<optionListItem> {
            var result = mutableListOf<optionListItem>()
            val bikeMode = carTypeWithIdx(GlobalApplication.prefs.getCarType()) == KNCarType.KNCarType_Bike

            if (bikeMode) {
                avoidOptionValue = avoidOptionValue or KNRouteAvoidOption.KNRouteAvoidOption_MotorWay.value
            }

            val category = getRouteOptionText()

            for ((key, value) in category) {
                result.add(optionListItem(list_Header, key))

                val option = value
                for ((optKey, optValue) in option) {
                    if (optKey is KNRouteAvoidOption) {
                        result.add(optionListItem(list_avoid, optValue, optKey))

                    } else if (optKey is KNRoutePriority) {
                        if (checkAvoidOption(KNRouteAvoidOption.KNRouteAvoidOption_MotorWay)) {
                            if (optKey == KNRoutePriority.KNRoutePriority_HighWay) {
                                continue
                            }
                        }

                        result.add(optionListItem(list_route, optValue, null, optKey))
                    }
                }
            }

            return result
        }

        init {
            viewList = setSectionList()
        }

        private lateinit var searchPoi: KNSearchPOI

        //선택된 장소 POI
        fun setSearchPoi(poi: KNSearchPOI) {
            searchPoi = poi
        }

        private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                list_Header -> categoryHolder(parent.inflate(R.layout.fragment_search_result_header))
                else -> routeHolder(parent.inflate(R.layout.fragment_search_route_option))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return viewList[position].viewType
        }

        override fun getItemCount(): Int {
            return viewList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = viewList[position]

            when (item.viewType) {
                list_Header -> {
                    (holder as categoryHolder).header_name.text = item.displayName
                }

                list_avoid -> {
                    var itemHolder = (holder as routeHolder)
                    itemHolder.poi_name.text = item.displayName
                    itemHolder.btnSelect.visibility = View.GONE
                    itemHolder.check.visibility = View.VISIBLE
                    itemHolder.check.isChecked = checkAvoidOption(item.avoidOption!!)
                    itemHolder.check.isSelected = itemHolder.check.isChecked
                    itemHolder.check.setOnClickListener{ v ->
                        val pos = holder.getAdapterPosition()
                        selectAvoidOption(v, viewList[pos].avoidOption!!)
                        dataChange()
                    }
                }

                list_route -> {
                    var itemHolder = (holder as routeHolder)
                    itemHolder.poi_name.text = item.displayName
                    itemHolder.btnSelect.visibility = View.VISIBLE
                    itemHolder.check.visibility = View.GONE
                    itemHolder.btnSelect.setOnClickListener{ v ->
                        val pos = holder.getAdapterPosition()
//                        optionListener?.onSearchResult(searchPoi, viewList[pos].avoidOption, viewList[pos].routeOption)
                        optionListener?.onSearchResult(
                            poi = searchPoi,
                            avoidOption = avoidOptionValue,
                            routeOption = viewList[pos].routeOption)
                    }
                }
            }
        }

        fun dataChange() {
            viewList.clear()
            viewList.addAll(setSectionList())
            notifyDataSetChanged()
        }

        private fun selectAvoidOption(v: View, option: KNRouteAvoidOption) {
            val selected = v.isSelected != true
            v.isSelected = selected
            if (selected) {
                avoidOptionValue = avoidOptionValue or option.value
            } else {
                avoidOptionValue = avoidOptionValue xor option.value
            }
        }

        private fun checkAvoidOption(checkValue: KNRouteAvoidOption) :Boolean {
            return (avoidOptionValue and checkValue.value) == checkValue.value
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

        binding.routeOptionList.visibility = View.GONE

        binding.txtError.visibility = View.GONE
    }
}