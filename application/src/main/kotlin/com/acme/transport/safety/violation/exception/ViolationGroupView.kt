package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation group. */
data class ViolationGroupView(
    var codeText: String,
    var title: String,
    var durationMinutes: String,
    var endsAtText: String,
    var odometerText: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toViolationGroupView(item: ViolationGroup): ViolationGroupView {
    return ViolationGroupView(
        codeText = truncate(item.code, 64),
        title = item.title,
        durationMinutes = item.durationMinutes.toString(),
        endsAtText = item.endsAt.toString(),
        odometerText = item.odometer.toString(),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationGroupViews(items: MutableList<ViolationGroup>): MutableList<ViolationGroupView> {
    val views = mutableListOf<ViolationGroupView>()
    for (item in items) {
        views.add(toViolationGroupView(item))
    }
    return views
}

fun sumViolationGroupView(view: ViolationGroupView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble() + parsedOdometer.toDouble()
}
