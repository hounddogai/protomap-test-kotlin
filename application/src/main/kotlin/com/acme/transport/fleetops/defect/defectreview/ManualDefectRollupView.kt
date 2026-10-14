package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual defect rollup. */
data class ManualDefectRollupView(
    var code: String,
    var label: String,
    var graceDaysText: String,
    var phoneText: String,
    var loadKg: String,
    var durationMinutes: String,
    var segmentText: String,
    var activeText: String,
    var priority: String,
    var statusLabel: String,
)

fun toManualDefectRollupView(item: ManualDefectRollup): ManualDefectRollupView {
    return ManualDefectRollupView(
        code = item.code,
        label = truncate(item.label, 64),
        graceDaysText = item.graceDays.toString(),
        phoneText = truncate(item.phone, 80),
        loadKg = "%.2f".format(item.loadKg),
        durationMinutes = item.durationMinutes.toString(),
        segmentText = item.segment,
        activeText = "${item.active}",
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDefectRollupViews(items: MutableList<ManualDefectRollup>): MutableList<ManualDefectRollupView> {
    val views = mutableListOf<ManualDefectRollupView>()
    for (item in items) {
        views.add(toManualDefectRollupView(item))
    }
    return views
}

fun sumManualDefectRollupView(view: ManualDefectRollupView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedGraceDays.toDouble() + parsedLoadKg + parsedDurationMinutes.toDouble()
}
