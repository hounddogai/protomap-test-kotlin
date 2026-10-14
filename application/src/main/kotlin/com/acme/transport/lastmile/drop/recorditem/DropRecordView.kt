package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop record. */
data class DropRecordView(
    var codeText: String,
    var label: String,
    var maximum: String,
    var unit: String,
    var threshold: String,
    var minimum: String,
    var ratio: String,
    var statusLabel: String,
)

fun toDropRecordView(item: DropRecord): DropRecordView {
    return DropRecordView(
        codeText = truncate(item.code, 120),
        label = truncate(item.label, 120),
        maximum = "%.2f".format(item.maximum),
        unit = item.unit,
        threshold = "%.2f".format(item.threshold),
        minimum = "%.2f".format(item.minimum),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropRecordViews(items: MutableList<DropRecord>): MutableList<DropRecordView> {
    val views = mutableListOf<DropRecordView>()
    for (item in items) {
        views.add(toDropRecordView(item))
    }
    return views
}

fun sumDropRecordView(view: DropRecordView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedMaximum + parsedThreshold + parsedMinimum
}
