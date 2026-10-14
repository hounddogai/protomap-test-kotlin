package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive inspection milestone. */
data class AdaptiveInspectionMilestoneView(
    var code: String,
    var label: String,
    var summary: String,
    var countryText: String,
    var region: String,
    var maximum: String,
    var createdAt: String,
    var unit: String,
    var ratio: String,
    var carrier: String,
    var checksum: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionMilestoneView(item: AdaptiveInspectionMilestone): AdaptiveInspectionMilestoneView {
    return AdaptiveInspectionMilestoneView(
        code = truncate(item.code, 40),
        label = item.label,
        summary = item.summary,
        countryText = item.country,
        region = item.region,
        maximum = "%.2f".format(item.maximum),
        createdAt = item.createdAt.toString(),
        unit = item.unit,
        ratio = "%.2f".format(item.ratio),
        carrier = item.carrier,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionMilestoneViews(items: MutableList<AdaptiveInspectionMilestone>): MutableList<AdaptiveInspectionMilestoneView> {
    val views = mutableListOf<AdaptiveInspectionMilestoneView>()
    for (item in items) {
        views.add(toAdaptiveInspectionMilestoneView(item))
    }
    return views
}

fun sumAdaptiveInspectionMilestoneView(view: AdaptiveInspectionMilestoneView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedMaximum + parsedCreatedAt.toDouble() + parsedRatio
}
