package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted parcel forecast. */
data class WeightedParcelForecastView(
    var code: String,
    var title: String,
    var odometerText: String,
    var expiresAt: String,
    var shiftCodeText: String,
    var checksum: String,
    var lane: String,
    var depot: String,
    var description: String,
    var timezone: String,
    var statusLabel: String,
)

fun toWeightedParcelForecastView(item: WeightedParcelForecast): WeightedParcelForecastView {
    return WeightedParcelForecastView(
        code = item.code,
        title = item.title,
        odometerText = item.odometer.toString(),
        expiresAt = item.expiresAt.toString(),
        shiftCodeText = truncate(item.shiftCode, 80),
        checksum = item.checksum,
        lane = item.lane,
        depot = item.depot,
        description = item.description,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedParcelForecastViews(items: MutableList<WeightedParcelForecast>): MutableList<WeightedParcelForecastView> {
    val views = mutableListOf<WeightedParcelForecastView>()
    for (item in items) {
        views.add(toWeightedParcelForecastView(item))
    }
    return views
}

fun sumWeightedParcelForecastView(view: WeightedParcelForecastView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedOdometer.toDouble() + parsedExpiresAt.toDouble()
}
