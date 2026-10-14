package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending swap record. */
data class PendingSwapRecordView(
    var code: String,
    var label: String,
    var odometer: String,
    var distanceKm: String,
    var priority: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toPendingSwapRecordView(item: PendingSwapRecord): PendingSwapRecordView {
    return PendingSwapRecordView(
        code = item.code,
        label = truncate(item.label, 40),
        odometer = item.odometer.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        priority = item.priority.toString(),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingSwapRecordViews(items: MutableList<PendingSwapRecord>): MutableList<PendingSwapRecordView> {
    val views = mutableListOf<PendingSwapRecordView>()
    for (item in items) {
        views.add(toPendingSwapRecordView(item))
    }
    return views
}

fun sumPendingSwapRecordView(view: PendingSwapRecordView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedOdometer.toDouble() + parsedDistanceKm + parsedPriority.toDouble()
}
