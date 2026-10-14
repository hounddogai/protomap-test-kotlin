package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect bundle. */
data class DefectBundleView(
    var code: String,
    var name: String,
    var thresholdText: String,
    var ceilingText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toDefectBundleView(item: DefectBundle): DefectBundleView {
    return DefectBundleView(
        code = item.code,
        name = truncate(item.name, 120),
        thresholdText = "%.2f".format(item.threshold),
        ceilingText = "%.2f".format(item.ceiling),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectBundleViews(items: MutableList<DefectBundle>): MutableList<DefectBundleView> {
    val views = mutableListOf<DefectBundleView>()
    for (item in items) {
        views.add(toDefectBundleView(item))
    }
    return views
}

fun sumDefectBundleView(view: DefectBundleView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedThreshold + parsedCeiling + parsedDurationMinutes.toDouble()
}
