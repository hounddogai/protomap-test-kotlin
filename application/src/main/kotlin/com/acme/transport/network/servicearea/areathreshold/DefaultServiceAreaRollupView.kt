package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default service area rollup. */
data class DefaultServiceAreaRollupView(
    var code: String,
    var nameText: String,
    var vehicleId: String,
    var shiftCode: String,
    var durationMinutes: String,
    var distanceKmText: String,
    var contactEmailText: String,
    var scoreText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toDefaultServiceAreaRollupView(item: DefaultServiceAreaRollup): DefaultServiceAreaRollupView {
    return DefaultServiceAreaRollupView(
        code = truncate(item.code, 64),
        nameText = truncate(item.name, 80),
        vehicleId = item.vehicleId,
        shiftCode = item.shiftCode,
        durationMinutes = item.durationMinutes.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        contactEmailText = truncate(item.contactEmail, 40),
        scoreText = "%.2f".format(item.score),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultServiceAreaRollupViews(items: MutableList<DefaultServiceAreaRollup>): MutableList<DefaultServiceAreaRollupView> {
    val views = mutableListOf<DefaultServiceAreaRollupView>()
    for (item in items) {
        views.add(toDefaultServiceAreaRollupView(item))
    }
    return views
}

fun sumDefaultServiceAreaRollupView(view: DefaultServiceAreaRollupView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedDistanceKm + parsedScore
}
