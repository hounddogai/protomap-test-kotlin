package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive inspection policy. */
data class AdaptiveInspectionPolicyView(
    var code: String,
    var name: String,
    var margin: String,
    var odometerText: String,
    var lane: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionPolicyView(item: AdaptiveInspectionPolicy): AdaptiveInspectionPolicyView {
    return AdaptiveInspectionPolicyView(
        code = item.code,
        name = item.name,
        margin = "%.2f".format(item.margin),
        odometerText = item.odometer.toString(),
        lane = truncate(item.lane, 120),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionPolicyViews(items: MutableList<AdaptiveInspectionPolicy>): MutableList<AdaptiveInspectionPolicyView> {
    val views = mutableListOf<AdaptiveInspectionPolicyView>()
    for (item in items) {
        views.add(toAdaptiveInspectionPolicyView(item))
    }
    return views
}

fun sumAdaptiveInspectionPolicyView(view: AdaptiveInspectionPolicyView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedMargin + parsedOdometer.toDouble()
}
