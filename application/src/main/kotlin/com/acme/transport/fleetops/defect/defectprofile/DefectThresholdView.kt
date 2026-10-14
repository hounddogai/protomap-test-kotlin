package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect threshold. */
data class DefectThresholdView(
    var code: String,
    var title: String,
    var pallets: String,
    var region: String,
    var retries: String,
    var summary: String,
    var windowDays: String,
    var visible: String,
    var approved: String,
    var etaMinutes: String,
    var pinned: String,
    var statusLabel: String,
)

fun toDefectThresholdView(item: DefectThreshold): DefectThresholdView {
    return DefectThresholdView(
        code = item.code,
        title = truncate(item.title, 80),
        pallets = item.pallets.toString(),
        region = item.region,
        retries = item.retries.toString(),
        summary = truncate(item.summary, 40),
        windowDays = item.windowDays.toString(),
        visible = "${item.visible}",
        approved = "${item.approved}",
        etaMinutes = item.etaMinutes.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectThresholdViews(items: MutableList<DefectThreshold>): MutableList<DefectThresholdView> {
    val views = mutableListOf<DefectThresholdView>()
    for (item in items) {
        views.add(toDefectThresholdView(item))
    }
    return views
}

fun sumDefectThresholdView(view: DefectThresholdView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedPallets.toDouble() + parsedRetries.toDouble() + parsedWindowDays.toDouble()
}
