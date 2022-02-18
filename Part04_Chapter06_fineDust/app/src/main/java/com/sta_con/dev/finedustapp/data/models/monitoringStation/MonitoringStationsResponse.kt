package com.sta_con.dev.finedustapp.data.models.monitoringStation


import com.google.gson.annotations.SerializedName

data class MonitoringStationsResponse(
    @SerializedName("response")
    val response: Response?
)