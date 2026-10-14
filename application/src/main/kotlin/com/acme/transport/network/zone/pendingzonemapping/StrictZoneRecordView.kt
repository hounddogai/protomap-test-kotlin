package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict zone record. */
data class StrictZoneRecordView(
    var code: String,
    var title: String,
    var stopCount: String,
    var vehicleId: String,
    var loadKg: String,
    var quantity: String,
    var contactEmail: String,
    var budget: String,
    var statusLabel: String,
)

fun toStrictZoneRecordView(item: StrictZoneRecord): StrictZoneRecordView {
    return StrictZoneRecordView(
        code = item.code,
        title = item.title,
        stopCount = item.stopCount.toString(),
        vehicleId = truncate(item.vehicleId, 80),
        loadKg = "%.2f".format(item.loadKg),
        quantity = item.quantity.toString(),
        contactEmail = item.contactEmail,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictZoneRecordViews(items: MutableList<StrictZoneRecord>): MutableList<StrictZoneRecordView> {
    val views = mutableListOf<StrictZoneRecordView>()
    for (item in items) {
        views.add(toStrictZoneRecordView(item))
    }
    return views
}

fun sumStrictZoneRecordView(view: StrictZoneRecordView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedStopCount.toDouble() + parsedLoadKg + parsedQuantity.toDouble()
}
