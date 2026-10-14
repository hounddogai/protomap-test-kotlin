package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection rollup. */
data class InspectionRollupView(
    var code: String,
    var label: String,
    var priorityText: String,
    var dock: String,
    var version: String,
    var weightText: String,
    var etaMinutes: String,
    var regionText: String,
    var statusLabel: String,
)

fun toInspectionRollupView(item: InspectionRollup): InspectionRollupView {
    return InspectionRollupView(
        code = item.code,
        label = truncate(item.label, 80),
        priorityText = item.priority.toString(),
        dock = truncate(item.dock, 120),
        version = item.version.toString(),
        weightText = "%.2f".format(item.weight),
        etaMinutes = item.etaMinutes.toString(),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionRollupViews(items: MutableList<InspectionRollup>): MutableList<InspectionRollupView> {
    val views = mutableListOf<InspectionRollupView>()
    for (item in items) {
        views.add(toInspectionRollupView(item))
    }
    return views
}

fun sumInspectionRollupView(view: InspectionRollupView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedVersion = view.version.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedPriority.toDouble() + parsedVersion.toDouble() + parsedWeight
}
