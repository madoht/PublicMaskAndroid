package com.gomsang.lab.publicmask.ui.map

import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.gomsang.lab.publicmask.R
import com.gomsang.lab.publicmask.base.BaseFragment
import com.gomsang.lab.publicmask.databinding.FragmentMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import androidx.navigation.fragment.navArgs
import com.gomsang.lab.publicmask.libs.constants.Logger
import com.gomsang.lab.publicmask.libs.datas.Stock
import com.naver.maps.map.MapFragment
import com.naver.maps.map.overlay.Marker
import io.nlopez.smartlocation.SmartLocation
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapFragment : BaseFragment<FragmentMapBinding, MapViewModel>(), OnMapReadyCallback {
    override val layoutResourceId: Int
        get() = R.layout.fragment_map
    override val viewModel: MapViewModel by viewModel()

    private val args: MapFragmentArgs by navArgs()

    val place by lazy {
        args.place
    }

    var map: NaverMap? = null

    var markerList = mutableMapOf<Marker, Stock>()

    override fun initStartView() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)


        /*    SmartLocation.with(context).location()
                .oneFix()
                .start {
                    currentLocation.latitude
                    currentLocation.longitude

                }*/
    }

    override fun initDataBinding() {
        viewModel.stockLiveData.observe(this, Observer {
            markerList.keys.forEach {
                it.map = null
            }
            markerList.clear()

            it.forEach {
                val marker = Marker()
                marker.position = LatLng(it.dealerLatitude, it.dealerLongitude)
                marker.map = map
                markerList.put(marker, it)
            }
        })
    }

    override fun initAfterBinding() {
    }

    override fun onMapReady(map: NaverMap) {
        this.map = map

        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isZoomControlEnabled = false
        map.uiSettings.isZoomGesturesEnabled = false

        // camera update
        val cameraUpdate =
            CameraUpdate.scrollTo(LatLng(place.latitude!!.toDouble(), place.longitude!!.toDouble()))
        map.moveCamera(cameraUpdate)

        // marker added
        val marker = Marker()
        marker.position = LatLng(place.latitude!!.toDouble(), place.longitude!!.toDouble())
        marker.map = map

        map.addOnCameraChangeListener { reason, animated ->
            Log.i("NaverMap", "카메라 변경 - reson: $reason, animated: $animated")
            val target = map.cameraPosition.target
            viewModel.query(target.latitude, target.longitude)
            Logger.log("cameraLog", target.latitude.toString() + " " + target.longitude.toString())
        }
    }
}