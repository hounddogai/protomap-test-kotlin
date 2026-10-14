package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling variance. */
data class IdlingVarianceView(
    var code: String,
    var name: String,
    var costText: String,
    var odometerText: String,
    var summaryText: String,
    var label: String,
    var distanceKmText: String,
    var ownerId: String,
    var shiftCode: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toIdlingVarianceView(item: IdlingVariance): IdlingVarianceView {
    return IdlingVarianceView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 64),
        costText = "%.2f".format(item.cost),
        odometerText = item.odometer.toString(),
        summaryText = truncate(item.summary, 64),
        label = item.label,
        distanceKmText = "%.2f".format(item.distanceKm),
        ownerId = item.ownerId,
        shiftCode = item.shiftCode,
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingVarianceViews(items: MutableList<IdlingVariance>): MutableList<IdlingVarianceView> {
    val views = mutableListOf<IdlingVarianceView>()
    for (item in items) {
        views.add(toIdlingVarianceView(item))
    }
    return views
}

fun sumIdlingVarianceView(view: IdlingVarianceView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedCost + parsedOdometer.toDouble() + parsedDistanceKm
}
