package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary parcel entry. */
data class PrimaryParcelEntryView(
    var code: String,
    var name: String,
    var routeCodeText: String,
    var durationMinutes: String,
    var weight: String,
    var vehicleId: String,
    var stopCount: String,
    var maximum: String,
    var budget: String,
    var statusLabel: String,
)

fun toPrimaryParcelEntryView(item: PrimaryParcelEntry): PrimaryParcelEntryView {
    return PrimaryParcelEntryView(
        code = item.code,
        name = item.name,
        routeCodeText = item.routeCode,
        durationMinutes = item.durationMinutes.toString(),
        weight = "%.2f".format(item.weight),
        vehicleId = truncate(item.vehicleId, 64),
        stopCount = item.stopCount.toString(),
        maximum = "%.2f".format(item.maximum),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryParcelEntryViews(items: MutableList<PrimaryParcelEntry>): MutableList<PrimaryParcelEntryView> {
    val views = mutableListOf<PrimaryParcelEntryView>()
    for (item in items) {
        views.add(toPrimaryParcelEntryView(item))
    }
    return views
}

fun sumPrimaryParcelEntryView(view: PrimaryParcelEntryView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedDurationMinutes.toDouble() + parsedWeight + parsedStopCount.toDouble()
}
