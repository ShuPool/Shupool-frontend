package com.hyundailogics.shupool.apitest

data class ResultAddress(
    var documents : List<GetAddress>
)

data class GetAddress(
    val road_address : RoadAddress,
)

data class RoadAddress(
    var address_name : String,
    var building_name : String
)