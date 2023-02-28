package com.kakaomobility.knsample.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.fragment.FragmentSearchListener
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSearchReqType
import com.kakaomobility.knsdk.api.objects.KNSearchResultObject
import com.kakaomobility.knsdk.common.objects.KNSearchPOI

const val FragmentSearchAdapterTypeHeader = 0
const val FragmentSearchAdapterTypeItem = 1
const val FragmentSearchAdapterTypeFooter = 2

interface FragmentSearchAdapterListener {
    fun onItemClick(poi: KNSearchPOI)
    fun onSearchMore(type: KNSearchReqType)
}

class FragmentSearchAdapter(private val listener: FragmentSearchAdapterListener, private val isSearchGoal: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val addresses: ArrayList<KNSearchPOI> = arrayListOf()
    private var hasHeaderOfAddress = 0
    private var hasFooterOfAddress = 0
    private var totalCntOfAddress = 0

    private val places: ArrayList<KNSearchPOI> = arrayListOf()
    private var hasHeaderOfPlaces = 0
    private var hasFooterOfPlaces = 0
    private var totalCntOfPlace = 0

    override fun getItemCount(): Int {
        return totalItemCnt()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val addressSize = addressItemCnt()
        var idx = position

        if (holder is HeaderViewHolder) {
            if (position >= addressSize) {
                holder.bind("장소결과")
            } else {
                holder.bind("주소결과")
            }
        } else if (holder is FooterViewHolder) {
            if (position >= addressSize) {
                holder.bind("장소결과 더보기", View.OnClickListener {
                    listener.onSearchMore(KNSearchReqType.KNSearchReqType_3)
                })
            } else {
                holder.bind("주소결과 더보기", View.OnClickListener {
                    listener.onSearchMore(KNSearchReqType.KNSearchReqType_2)
                })
            }
        } else if (holder is ItemViewHolder) {
            if (position >= addressSize) {
                idx -= (addressSize + hasHeaderOfPlaces)

                holder.bind(places[idx], isSearchGoal, object: FragmentSearchListener {
                    // 장소 결과 출발 버튼
                    override fun onSearchResult(poi: KNSearchPOI, avoidOption: Int, routeOption: KNRoutePriority?) {
                        listener.onItemClick(poi)
                        Log.d("장소 결과 출발 버튼", poi.name)
                    }
                })
            } else {
                idx -= hasHeaderOfAddress

                holder.bind(addresses[idx], isSearchGoal, object: FragmentSearchListener {
                    // 주소 결과 출발 버튼
                    override fun onSearchResult(poi: KNSearchPOI, avoidOption: Int, routeOption: KNRoutePriority?) {
                        listener.onItemClick(poi)
                        Log.d("주소 결과 출발 버튼", poi.name)
                    }
                })
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var type = FragmentSearchAdapterTypeItem
        val addressSize = addressItemCnt()
        var idx = position

        if (position >= addressSize) {
            idx -= addressSize

            if (hasHeaderOfPlaces == 1 && idx == 0) {
                type = FragmentSearchAdapterTypeHeader
            } else if (hasFooterOfPlaces == 1 && idx == (hasHeaderOfPlaces + places.size)) {
                type = FragmentSearchAdapterTypeFooter
            }
        } else {
            if (hasHeaderOfAddress == 1 && idx == 0) {
                type = FragmentSearchAdapterTypeHeader
            } else if (hasFooterOfAddress == 1 && idx == (hasHeaderOfAddress + addresses.size)) {
                type = FragmentSearchAdapterTypeFooter
            }
        }

        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FragmentSearchAdapterTypeHeader -> {
                HeaderViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.fragment_search_result_header, parent, false))
            }

            FragmentSearchAdapterTypeFooter -> {
                FooterViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.fragment_search_result_footer, parent, false))
            }

            else -> {
                ItemViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.fragment_search_result_item, parent, false))
            }
        }
    }

    fun clear() {
        val itemCnt = totalItemCnt()

        addresses.clear()
        hasHeaderOfAddress = 0
        hasFooterOfAddress = 0
        totalCntOfAddress = 0

        places.clear()
        hasHeaderOfPlaces = 0
        hasFooterOfPlaces = 0
        totalCntOfPlace = 0

        notifyItemRangeRemoved(0, itemCnt)
    }

    fun setAddressesData(obj: KNSearchResultObject) {
        with (obj) {
            val positionStart = addresses.size + hasHeaderOfAddress

            if (hasFooterOfAddress == 1 && isEnd) {
                notifyItemRemoved(positionStart)
            }

            hasFooterOfAddress = if (isEnd) 0 else 1
            totalCntOfAddress = totalCnt

            poiList?.let {
                if (!it.isNullOrEmpty()) {
                    val itemCnt = it.size + if (positionStart == 0) (1 + hasFooterOfAddress) else 0

                    addresses.addAll(it.toTypedArray())
                    notifyItemRangeInserted(positionStart, itemCnt)

                    hasHeaderOfAddress = if (addresses.size > 0) 1 else 0
                }
            }
        }
    }

    fun setPlacesData(obj: KNSearchResultObject) {
        with (obj) {
            val positionStart = places.size + hasHeaderOfPlaces

            if (hasFooterOfPlaces == 1 && isEnd) {
                notifyItemRemoved(positionStart + addressItemCnt())
            }

            hasFooterOfPlaces = if (isEnd) 0 else 1
            totalCntOfPlace = totalCnt

            poiList?.let {
                if (!it.isNullOrEmpty()) {
                    val itemCnt = it.size + if (positionStart == 0) (1 + hasFooterOfPlaces) else 0

                    places.addAll(it.toTypedArray())
                    notifyItemRangeInserted(positionStart + addressItemCnt(), itemCnt)

                    hasHeaderOfPlaces = if (places.size > 0) 1 else 0
                }
            }
        }
    }

    fun hasSearchResult() = addresses.size > 0 || places.size > 0
    private fun totalItemCnt() = addresses.size + places.size + hasHeaderOfAddress + hasHeaderOfPlaces + hasFooterOfAddress + hasFooterOfPlaces
    private fun addressItemCnt() = addresses.size + hasHeaderOfAddress + hasFooterOfAddress

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.findViewById<TextView>(R.id.header_name)

        fun bind(aName: String) {
            name.text = aName
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.findViewById<TextView>(R.id.poi_name)
        private val address = view.findViewById<TextView>(R.id.poi_address)
        private val btnSelect = view.findViewById<Button>(R.id.btnSelect)

        fun bind(item: KNSearchPOI, isSearchGoal: Boolean, listener: FragmentSearchListener) {
            name.text = item.name
            address.text = item.address

            itemView.setOnClickListener {
                Log.d("아이템", item.name)
            }

            address.visibility = if (name.text == address.text) View.INVISIBLE else View.VISIBLE

            with (btnSelect) {
                text = if (isSearchGoal) "출발" else "경유"
                this.setOnClickListener {
                    listener.onSearchResult(item)
                }
            }

        }
    }

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.findViewById<TextView>(R.id.footer_name)

        fun bind(aName: String, onClickListener: View.OnClickListener) {
            name.text = aName
            name.setOnClickListener(onClickListener)
        }
    }
}