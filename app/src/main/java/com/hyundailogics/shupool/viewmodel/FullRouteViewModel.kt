package com.kakaomobility.knsample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hyundailogics.shupool.KNToDistStr
import com.hyundailogics.shupool.KNToTimeStr
import com.hyundailogics.shupool.utils.Event
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute

data class FullRouteData(
    var startName: String? = null,
    var goalName: String? = null,
    var goalDist: String? = null,
    var goalTime: String? = null,
    var wayPoints: MutableList<WayPointInfos> = mutableListOf(),
    var pri_totalDist: String? = null,
    var pri_totalTime: String? = null,
    var sec_totalDist: String? = null,
    var sec_totalTime: String? = null
)

data class WayPointInfos(
    var name: String,
    var time: String,
    var dist: String
)

class FullRouteViewModel : ViewModel() {
    private val _fullRouteInfoData: MutableLiveData<FullRouteData> = MutableLiveData()
    val fullRouteInfoData: LiveData<FullRouteData> get() = _fullRouteInfoData

    private val _addEvent = MutableLiveData<Event<*>>()
    val addEvent: LiveData<Event<*>> get() = _addEvent

    private val _removeEvent = MutableLiveData<Event<Int>>()
    val removeEvent: LiveData<Event<Int>> get() = _removeEvent

    private val _prevEvent = MutableLiveData<Event<*>>()
    val prevEvent: LiveData<Event<*>> get() = _prevEvent

    private val _selectGuideEvent = MutableLiveData<Event<Boolean>>()
    val selectGuideEvent: LiveData<Event<Boolean>> get() = _selectGuideEvent

    private val _guideStartEvent = MutableLiveData<Event<Boolean>>()
    val guideStartEvent: LiveData<Event<Boolean>> get() = _guideStartEvent

    fun setFullRouteData(aTrip: KNTrip, aRoute: MutableList<KNRoute>) {
        _fullRouteInfoData.value = FullRouteData().apply {
            val endLocation = aRoute[0].mainDirectionList().last().location
            var tempLocation = aRoute[0].mainDirectionList().first().location

            if (aRoute[0].viaLocations != null && aRoute[0].viaLocations!!.isNotEmpty()) {
                val list = aRoute[0].viaLocations!!
                for (i in list.indices) {
                    val point = WayPointInfos(
                        aTrip.vias!![i].name,
                        KNToTimeStr(tempLocation.timeToLocation(list[i])),
                        KNToDistStr(tempLocation.distToLocation(list[i]))
                    )

                    wayPoints.add(point)
                    tempLocation = list[i]
                }
            }

            goalTime = KNToTimeStr(tempLocation.timeToLocation(endLocation))
            goalDist = KNToDistStr(tempLocation.distToLocation(endLocation))
            goalName = aTrip.goal.name

            pri_totalDist = KNToDistStr(aRoute[0].totalDist)
            pri_totalTime = KNToTimeStr(aRoute[0].totalTime)

            if (aRoute.size > 1) {
                sec_totalDist = KNToDistStr(aRoute[1].totalDist)
                sec_totalTime = KNToTimeStr(aRoute[1].totalTime)
            }
        }
    }

    fun onWayPointRemoveEvent(pos: Int) {
        _removeEvent.value = Event(pos)
    }

    fun onWayPointAddEvent() {
        _addEvent.value = Event(Any())
    }

    fun onPrevEvent() {
        _prevEvent.value = Event(Any())
    }

    fun onSelectGuideEvent(priRoute: Boolean) {
        _selectGuideEvent.value = Event(priRoute)
    }

    fun onGuideStartEvent(priRoute: Boolean) {
        _guideStartEvent.value = Event(priRoute)
    }
}