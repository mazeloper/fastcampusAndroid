package com.sta_con.dev.finedustapp.data.services

import com.sta_con.dev.finedustapp.BuildConfig
import com.sta_con.dev.finedustapp.data.models.airquality.AirQualityResponse
import com.sta_con.dev.finedustapp.data.models.monitoringStation.MonitoringStationsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AirKoreaApiService {

    @GET(
        "B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList" +
                "?serviceKey=${BuildConfig.AIR_KOREA_SERVICE_KEY}" +
                "&returnType=json"
    )
    suspend fun getNearbyMonitoringStation(
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double
    ): Response<MonitoringStationsResponse>

    @GET(
        "B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty" +
                "?serviceKey=${BuildConfig.AIR_KOREA_SERVICE_KEY}" +
                "&returnType=json" +
                "&dataTerm=DAILY" +
                "&var=1.3"
    )
    suspend fun getRealtimeAirQualities(
        @Query("stationName") stationName: String
    ) : Response<AirQualityResponse>
}