package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional utilization segment. */
data class RegionalUtilizationSegmentView(
    var codeText: String,
    var label: String,
    var expiresAtText: String,
    var pallets: String,
    var cost: String,
    var timezoneText: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toRegionalUtilizationSegmentView(item: RegionalUtilizationSegment): RegionalUtilizationSegmentView {
    return RegionalUtilizationSegmentView(
        codeText = truncate(item.code, 64),
        label = item.label,
        expiresAtText = item.expiresAt.toString(),
        pallets = item.pallets.toString(),
        cost = "%.2f".format(item.cost),
        timezoneText = item.timezone,
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalUtilizationSegmentViews(items: MutableList<RegionalUtilizationSegment>): MutableList<RegionalUtilizationSegmentView> {
    val views = mutableListOf<RegionalUtilizationSegmentView>()
    for (item in items) {
        views.add(toRegionalUtilizationSegmentView(item))
    }
    return views
}

fun sumRegionalUtilizationSegmentView(view: RegionalUtilizationSegmentView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedExpiresAt.toDouble() + parsedPallets.toDouble() + parsedCost
}
