package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped scorecard policy. */
data class CappedScorecardPolicyView(
    var code: String,
    var name: String,
    var version: String,
    var costText: String,
    var reviewer: String,
    var capacity: String,
    var createdAt: String,
    var distanceKm: String,
    var channelText: String,
    var sequence: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toCappedScorecardPolicyView(item: CappedScorecardPolicy): CappedScorecardPolicyView {
    return CappedScorecardPolicyView(
        code = truncate(item.code, 120),
        name = item.name,
        version = item.version.toString(),
        costText = "%.2f".format(item.cost),
        reviewer = item.reviewer,
        capacity = item.capacity.toString(),
        createdAt = item.createdAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        channelText = truncate(item.channel, 64),
        sequence = item.sequence.toString(),
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedScorecardPolicyViews(items: MutableList<CappedScorecardPolicy>): MutableList<CappedScorecardPolicyView> {
    val views = mutableListOf<CappedScorecardPolicyView>()
    for (item in items) {
        views.add(toCappedScorecardPolicyView(item))
    }
    return views
}

fun sumCappedScorecardPolicyView(view: CappedScorecardPolicyView): Double {
    val parsedVersion = view.version.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedVersion.toDouble() + parsedCost + parsedCapacity.toDouble()
}
