package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation target. */
data class ViolationTargetView(
    var code: String,
    var label: String,
    var shiftCode: String,
    var etaMinutesText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toViolationTargetView(item: ViolationTarget): ViolationTargetView {
    return ViolationTargetView(
        code = item.code,
        label = truncate(item.label, 120),
        shiftCode = item.shiftCode,
        etaMinutesText = item.etaMinutes.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationTargetViews(items: MutableList<ViolationTarget>): MutableList<ViolationTargetView> {
    val views = mutableListOf<ViolationTargetView>()
    for (item in items) {
        views.add(toViolationTargetView(item))
    }
    return views
}

fun sumViolationTargetView(view: ViolationTargetView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedEtaMinutes.toDouble() + parsedCapacity.toDouble()
}
