package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual dwell profile. */
data class ManualDwellProfileView(
    var code: String,
    var title: String,
    var margin: String,
    var floorValue: String,
    var summary: String,
    var durationMinutesText: String,
    var segmentText: String,
    var odometer: String,
    var unitText: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toManualDwellProfileView(item: ManualDwellProfile): ManualDwellProfileView {
    return ManualDwellProfileView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 64),
        margin = "%.2f".format(item.margin),
        floorValue = "%.2f".format(item.floorValue),
        summary = truncate(item.summary, 64),
        durationMinutesText = item.durationMinutes.toString(),
        segmentText = truncate(item.segment, 64),
        odometer = item.odometer.toString(),
        unitText = item.unit,
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDwellProfileViews(items: MutableList<ManualDwellProfile>): MutableList<ManualDwellProfileView> {
    val views = mutableListOf<ManualDwellProfileView>()
    for (item in items) {
        views.add(toManualDwellProfileView(item))
    }
    return views
}

fun sumManualDwellProfileView(view: ManualDwellProfileView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedMargin + parsedFloorValue + parsedDurationMinutes.toDouble()
}
