package com.sta_con.dev.finedustapp.data.models.monitoringStation


import com.google.gson.annotations.SerializedName

data class MonitoringStation(
    @SerializedName("addr")
    val addr: String?,
    @SerializedName("stationName")
    val stationName: String?,
    @SerializedName("tm")
    val tm: Double?
)