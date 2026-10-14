package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict lease return milestone. */
data class StrictLeaseReturnMilestoneView(
    var codeText: String,
    var label: String,
    var weightText: String,
    var version: String,
    var segment: String,
    var maximum: String,
    var statusLabel: String,
)

fun toStrictLeaseReturnMilestoneView(item: StrictLeaseReturnMilestone): StrictLeaseReturnMilestoneView {
    return StrictLeaseReturnMilestoneView(
        codeText = item.code,
        label = item.label,
        weightText = "%.2f".format(item.weight),
        version = item.version.toString(),
        segment = truncate(item.segment, 120),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictLeaseReturnMilestoneViews(items: MutableList<StrictLeaseReturnMilestone>): MutableList<StrictLeaseReturnMilestoneView> {
    val views = mutableListOf<StrictLeaseReturnMilestoneView>()
    for (item in items) {
        views.add(toStrictLeaseReturnMilestoneView(item))
    }
    return views
}

fun sumStrictLeaseReturnMilestoneView(view: StrictLeaseReturnMilestoneView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedWeight + parsedVersion.toDouble() + parsedMaximum
}
