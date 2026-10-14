package com.acme.transport.fleetops.leasereturn.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback lease return tier. */
data class FallbackLeaseReturnTierView(
    var codeText: String,
    var nameText: String,
    var maximumText: String,
    var priority: String,
    var vehicleId: String,
    var routeCode: String,
    var score: String,
    var unitText: String,
    var capacity: String,
    var channel: String,
    var segment: String,
    var statusLabel: String,
)

fun toFallbackLeaseReturnTierView(item: FallbackLeaseReturnTier): FallbackLeaseReturnTierView {
    return FallbackLeaseReturnTierView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        maximumText = "%.2f".format(item.maximum),
        priority = item.priority.toString(),
        vehicleId = item.vehicleId,
        routeCode = item.routeCode,
        score = "%.2f".format(item.score),
        unitText = item.unit,
        capacity = item.capacity.toString(),
        channel = item.channel,
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackLeaseReturnTierViews(items: MutableList<FallbackLeaseReturnTier>): MutableList<FallbackLeaseReturnTierView> {
    val views = mutableListOf<FallbackLeaseReturnTierView>()
    for (item in items) {
        views.add(toFallbackLeaseReturnTierView(item))
    }
    return views
}

fun sumFallbackLeaseReturnTierView(view: FallbackLeaseReturnTierView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedScore = view.score.toDouble()
    return parsedMaximum + parsedPriority.toDouble() + parsedScore
}
