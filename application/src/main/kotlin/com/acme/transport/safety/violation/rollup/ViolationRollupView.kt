package com.acme.transport.safety.violation.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation rollup. */
data class ViolationRollupView(
    var code: String,
    var labelText: String,
    var priority: String,
    var distanceKm: String,
    var stopCount: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toViolationRollupView(item: ViolationRollup): ViolationRollupView {
    return ViolationRollupView(
        code = item.code,
        labelText = truncate(item.label, 120),
        priority = item.priority.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        stopCount = item.stopCount.toString(),
        reviewer = truncate(item.reviewer, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationRollupViews(items: MutableList<ViolationRollup>): MutableList<ViolationRollupView> {
    val views = mutableListOf<ViolationRollupView>()
    for (item in items) {
        views.add(toViolationRollupView(item))
    }
    return views
}

fun sumViolationRollupView(view: ViolationRollupView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedPriority.toDouble() + parsedDistanceKm + parsedStopCount.toDouble()
}
