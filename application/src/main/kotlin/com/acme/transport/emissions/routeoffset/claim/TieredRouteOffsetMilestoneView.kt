package com.acme.transport.emissions.routeoffset.claim

/** How screens and exports show a tiered route offset milestone. */
data class TieredRouteOffsetMilestoneView(
    var code: String,
    var title: String,
    var createdAt: String,
    var unit: String,
    var currency: String,
    var discountText: String,
    var amount: String,
    var statusLabel: String,
)

fun toTieredRouteOffsetMilestoneView(item: TieredRouteOffsetMilestone): TieredRouteOffsetMilestoneView {
    return TieredRouteOffsetMilestoneView(
        code = item.code,
        title = item.title,
        createdAt = item.createdAt.toString(),
        unit = item.unit,
        currency = item.currency,
        discountText = "%.2f".format(item.discount),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRouteOffsetMilestoneViews(items: MutableList<TieredRouteOffsetMilestone>): MutableList<TieredRouteOffsetMilestoneView> {
    val views = mutableListOf<TieredRouteOffsetMilestoneView>()
    for (item in items) {
        views.add(toTieredRouteOffsetMilestoneView(item))
    }
    return views
}

fun sumTieredRouteOffsetMilestoneView(view: TieredRouteOffsetMilestoneView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedDiscount = view.discountText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedCreatedAt.toDouble() + parsedDiscount + parsedAmount
}
