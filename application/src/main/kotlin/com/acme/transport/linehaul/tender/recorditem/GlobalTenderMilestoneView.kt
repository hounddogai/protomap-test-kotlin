package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global tender milestone. */
data class GlobalTenderMilestoneView(
    var code: String,
    var label: String,
    var cost: String,
    var channel: String,
    var ceilingText: String,
    var carrier: String,
    var lane: String,
    var statusLabel: String,
)

fun toGlobalTenderMilestoneView(item: GlobalTenderMilestone): GlobalTenderMilestoneView {
    return GlobalTenderMilestoneView(
        code = item.code,
        label = item.label,
        cost = "%.2f".format(item.cost),
        channel = truncate(item.channel, 40),
        ceilingText = "%.2f".format(item.ceiling),
        carrier = item.carrier,
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTenderMilestoneViews(items: MutableList<GlobalTenderMilestone>): MutableList<GlobalTenderMilestoneView> {
    val views = mutableListOf<GlobalTenderMilestoneView>()
    for (item in items) {
        views.add(toGlobalTenderMilestoneView(item))
    }
    return views
}

fun sumGlobalTenderMilestoneView(view: GlobalTenderMilestoneView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedCost + parsedCeiling
}
