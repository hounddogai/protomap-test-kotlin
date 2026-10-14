package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped dispute group. */
data class CappedDisputeGroupView(
    var code: String,
    var title: String,
    var minimum: String,
    var cost: String,
    var driverId: String,
    var reviewer: String,
    var score: String,
    var windowDaysText: String,
    var rate: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toCappedDisputeGroupView(item: CappedDisputeGroup): CappedDisputeGroupView {
    return CappedDisputeGroupView(
        code = item.code,
        title = truncate(item.title, 64),
        minimum = "%.2f".format(item.minimum),
        cost = "%.2f".format(item.cost),
        driverId = truncate(item.driverId, 120),
        reviewer = truncate(item.reviewer, 120),
        score = "%.2f".format(item.score),
        windowDaysText = item.windowDays.toString(),
        rate = "%.2f".format(item.rate),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDisputeGroupViews(items: MutableList<CappedDisputeGroup>): MutableList<CappedDisputeGroupView> {
    val views = mutableListOf<CappedDisputeGroupView>()
    for (item in items) {
        views.add(toCappedDisputeGroupView(item))
    }
    return views
}

fun sumCappedDisputeGroupView(view: CappedDisputeGroupView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedMinimum + parsedCost + parsedScore
}
