package com.sta_con.dev.finedustapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.sta_con.dev.finedustapp.data.Repository
import com.sta_con.dev.finedustapp.data.models.airquality.Grade
import com.sta_con.dev.finedustapp.data.models.airquality.MeasuredValue
import com.sta_con.dev.finedustapp.data.models.monitoringStation.MonitoringStation
import com.sta_con.dev.finedustapp.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindViews()
        initVariables()
        requestLocationPermissions()
    }

    private fun bindViews() {
        binding.refresh.setOnRefreshListener {
            fetchAirQualityData()
        }
    }

    private fun initVariables() {
        // Google Play Service 설치되어있을때만 가능
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_ACCESS_LOCATION_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val locationPermissionGranted =
            requestCode == REQUEST_ACCESS_LOCATION_PERMISSIONS && grantResults[0] == PackageManager.PERMISSION_GRANTED

        if (locationPermissionGranted.not()) {
            finish()
        } else {
            // 위치정보 & 미세먼지 측정 API
            fetchAirQualityData()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchAirQualityData() {
        cancellationTokenSource = CancellationTokenSource()

        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location ->
            scope.launch {
                binding.tvErrorDesc.isVisible = false
                try {
                    val monitoringStation = Repository.getNearbyMonitoringStation(location.latitude, location.longitude)

                    val measuredValue = monitoringStation?.stationName?.let {
                        Repository.getLatestAirQualityData(it)
                    }
                    displayAirQualityData(monitoringStation!!, measuredValue!!)
                } catch (e: Exception) {
                    binding.tvErrorDesc.isVisible = true
                    binding.contentLayout.alpha = 0f
                } finally {
                    binding.progressBar.isVisible = false
                    binding.refresh.isRefreshing = false
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayAirQualityData(monitoringStation: MonitoringStation, measuredValue: MeasuredValue) {
        binding.contentLayout.animate()
            .alpha(1f)
            .start()

        binding.tvMeasuringStationName.text = monitoringStation.stationName
        binding.tvMeasuringStationAddress.text = monitoringStation.addr

        (measuredValue.khaiGrade ?: Grade.UNKNOWN).let { grade ->
            binding.root.setBackgroundResource(grade.color)
            binding.tvTotalGradeLabel.text = grade.label
            binding.tvEmoji.text = grade.emoji
        }

        with(measuredValue) {
            binding.tvFineDustInfo.text = "미세먼지 : ${(pm10Value ?: "???")} ㎍/㎥ ${(pm10Grade ?: Grade.UNKNOWN).emoji}"
            binding.tvUltraFineDustInfo.text = "초미세먼지 : ${(pm25Value ?: "???")} ㎍/㎥ ${(pm25Grade ?: Grade.UNKNOWN).emoji}"

            with(binding.so2Item) {
                tvLabel.text = "아황산가스"
                tvGrade.text = (so2Grade ?: Grade.UNKNOWN).toString()
                tvValue.text = "$so2Value ppm"
            }
            with(binding.coItem) {
                tvLabel.text = "일산화탄소"
                tvGrade.text = (coGrade ?: Grade.UNKNOWN).toString()
                tvValue.text = "$so2Value ppm"
            }
            with(binding.o3Item) {
                tvLabel.text = "오존"
                tvGrade.text = (o3Grade ?: Grade.UNKNOWN).toString()
                tvValue.text = "$o3Value ppm"
            }
            with(binding.no2Item) {
                tvLabel.text = "이산화질소"
                tvGrade.text = (no2Grade ?: Grade.UNKNOWN).toString()
                tvValue.text = "$no2Value ppm"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.cancel()
        scope.cancel()
    }

    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
    }
}