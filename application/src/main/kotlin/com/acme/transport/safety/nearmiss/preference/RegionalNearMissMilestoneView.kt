package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional near miss milestone. */
data class RegionalNearMissMilestoneView(
    var code: String,
    var name: String,
    var segment: String,
    var lane: String,
    var amount: String,
    var active: String,
    var limitText: String,
    var ownerId: String,
    var shiftCode: String,
    var archived: String,
    var statusLabel: String,
)

fun toRegionalNearMissMilestoneView(item: RegionalNearMissMilestone): RegionalNearMissMilestoneView {
    return RegionalNearMissMilestoneView(
        code = item.code,
        name = item.name,
        segment = truncate(item.segment, 80),
        lane = item.lane,
        amount = "%.2f".format(item.amount),
        active = "${item.active}",
        limitText = item.limit.toString(),
        ownerId = truncate(item.ownerId, 64),
        shiftCode = truncate(item.shiftCode, 40),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalNearMissMilestoneViews(items: MutableList<RegionalNearMissMilestone>): MutableList<RegionalNearMissMilestoneView> {
    val views = mutableListOf<RegionalNearMissMilestoneView>()
    for (item in items) {
        views.add(toRegionalNearMissMilestoneView(item))
    }
    return views
}

fun sumRegionalNearMissMilestoneView(view: RegionalNearMissMilestoneView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedAmount + parsedLimit.toDouble()
}
