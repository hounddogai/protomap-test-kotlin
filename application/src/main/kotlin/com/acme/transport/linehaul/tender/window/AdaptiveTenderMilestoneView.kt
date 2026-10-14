package com.acme.transport.linehaul.tender.window

/** How screens and exports show a adaptive tender milestone. */
data class AdaptiveTenderMilestoneView(
    var code: String,
    var title: String,
    var lane: String,
    var amount: String,
    var cost: String,
    var approved: String,
    var rate: String,
    var description: String,
    var weight: String,
    var ratio: String,
    var statusLabel: String,
)

fun toAdaptiveTenderMilestoneView(item: AdaptiveTenderMilestone): AdaptiveTenderMilestoneView {
    return AdaptiveTenderMilestoneView(
        code = item.code,
        title = item.title,
        lane = item.lane,
        amount = "%.2f".format(item.amount),
        cost = "%.2f".format(item.cost),
        approved = "${item.approved}",
        rate = "%.2f".format(item.rate),
        description = item.description,
        weight = "%.2f".format(item.weight),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTenderMilestoneViews(items: MutableList<AdaptiveTenderMilestone>): MutableList<AdaptiveTenderMilestoneView> {
    val views = mutableListOf<AdaptiveTenderMilestoneView>()
    for (item in items) {
        views.add(toAdaptiveTenderMilestoneView(item))
    }
    return views
}

fun sumAdaptiveTenderMilestoneView(view: AdaptiveTenderMilestoneView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedAmount + parsedCost + parsedRate
}
