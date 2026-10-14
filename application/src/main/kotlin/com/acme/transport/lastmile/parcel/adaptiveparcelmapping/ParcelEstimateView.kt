package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel estimate. */
data class ParcelEstimateView(
    var code: String,
    var label: String,
    var budget: String,
    var maximum: String,
    var revision: String,
    var startsAt: String,
    var region: String,
    var fuelLiters: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toParcelEstimateView(item: ParcelEstimate): ParcelEstimateView {
    return ParcelEstimateView(
        code = item.code,
        label = truncate(item.label, 40),
        budget = "%.2f".format(item.budget),
        maximum = "%.2f".format(item.maximum),
        revision = item.revision.toString(),
        startsAt = item.startsAt.toString(),
        region = truncate(item.region, 64),
        fuelLiters = "%.2f".format(item.fuelLiters),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelEstimateViews(items: MutableList<ParcelEstimate>): MutableList<ParcelEstimateView> {
    val views = mutableListOf<ParcelEstimateView>()
    for (item in items) {
        views.add(toParcelEstimateView(item))
    }
    return views
}

fun sumParcelEstimateView(view: ParcelEstimateView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedBudget + parsedMaximum + parsedRevision.toDouble()
}
