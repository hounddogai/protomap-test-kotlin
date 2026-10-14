package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization group. */
data class UtilizationGroupView(
    var code: String,
    var title: String,
    var discount: String,
    var reviewerText: String,
    var trailerId: String,
    var pinned: String,
    var updatedAt: String,
    var currency: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toUtilizationGroupView(item: UtilizationGroup): UtilizationGroupView {
    return UtilizationGroupView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 80),
        discount = "%.2f".format(item.discount),
        reviewerText = item.reviewer,
        trailerId = item.trailerId,
        pinned = "${item.pinned}",
        updatedAt = item.updatedAt.toString(),
        currency = item.currency,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationGroupViews(items: MutableList<UtilizationGroup>): MutableList<UtilizationGroupView> {
    val views = mutableListOf<UtilizationGroupView>()
    for (item in items) {
        views.add(toUtilizationGroupView(item))
    }
    return views
}

fun sumUtilizationGroupView(view: UtilizationGroupView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedDiscount + parsedUpdatedAt.toDouble() + parsedScore
}
