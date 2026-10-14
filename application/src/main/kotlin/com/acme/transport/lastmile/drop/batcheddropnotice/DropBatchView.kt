package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop batch. */
data class DropBatchView(
    var code: String,
    var title: String,
    var ratioText: String,
    var checksum: String,
    var durationMinutes: String,
    var odometer: String,
    var costText: String,
    var statusLabel: String,
)

fun toDropBatchView(item: DropBatch): DropBatchView {
    return DropBatchView(
        code = item.code,
        title = truncate(item.title, 120),
        ratioText = "%.2f".format(item.ratio),
        checksum = truncate(item.checksum, 40),
        durationMinutes = item.durationMinutes.toString(),
        odometer = item.odometer.toString(),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropBatchViews(items: MutableList<DropBatch>): MutableList<DropBatchView> {
    val views = mutableListOf<DropBatchView>()
    for (item in items) {
        views.add(toDropBatchView(item))
    }
    return views
}

fun sumDropBatchView(view: DropBatchView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedRatio + parsedDurationMinutes.toDouble() + parsedOdometer.toDouble()
}
