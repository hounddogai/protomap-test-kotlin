package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time entry. */
data class TransitTimeEntryView(
    var codeText: String,
    var titleText: String,
    var startsAtText: String,
    var carrierText: String,
    var driverIdText: String,
    var categoryText: String,
    var batchSize: String,
    var minimum: String,
    var vehicleId: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toTransitTimeEntryView(item: TransitTimeEntry): TransitTimeEntryView {
    return TransitTimeEntryView(
        codeText = item.code,
        titleText = item.title,
        startsAtText = item.startsAt.toString(),
        carrierText = item.carrier,
        driverIdText = item.driverId,
        categoryText = truncate(item.category, 80),
        batchSize = item.batchSize.toString(),
        minimum = "%.2f".format(item.minimum),
        vehicleId = item.vehicleId,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeEntryViews(items: MutableList<TransitTimeEntry>): MutableList<TransitTimeEntryView> {
    val views = mutableListOf<TransitTimeEntryView>()
    for (item in items) {
        views.add(toTransitTimeEntryView(item))
    }
    return views
}

fun sumTransitTimeEntryView(view: TransitTimeEntryView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedStartsAt.toDouble() + parsedBatchSize.toDouble() + parsedMinimum
}
