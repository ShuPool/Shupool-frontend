package com.hyundailogics.shupool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hyundailogics.shupool.activity.CarpoolJoinActivity
import com.hyundailogics.shupool.databinding.FragmentSetPickupBinding
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_X
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_Y
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView

class SetPickupFragment : Fragment() {
    lateinit var binding: FragmentSetPickupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetPickupBinding.inflate(inflater, container, false)

        initMapView(binding.mapView)

        binding.RequestButton.setOnClickListener {
            val activity = activity as CarpoolJoinActivity?
            activity?.finish()
        }

        binding.backBtn.setOnClickListener {
            val activity = activity as CarpoolJoinActivity?
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
            activity?.supportFragmentManager?.popBackStack()
        }

        return binding.root
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
}