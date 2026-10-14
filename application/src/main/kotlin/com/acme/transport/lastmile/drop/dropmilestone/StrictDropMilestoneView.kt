package com.acme.transport.lastmile.drop.dropmilestone

/** How screens and exports show a strict drop milestone. */
data class StrictDropMilestoneView(
    var code: String,
    var nameText: String,
    var windowDays: String,
    var rate: String,
    var discount: String,
    var locale: String,
    var phoneText: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toStrictDropMilestoneView(item: StrictDropMilestone): StrictDropMilestoneView {
    return StrictDropMilestoneView(
        code = item.code,
        nameText = item.name,
        windowDays = item.windowDays.toString(),
        rate = "%.2f".format(item.rate),
        discount = "%.2f".format(item.discount),
        locale = item.locale,
        phoneText = item.phone,
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDropMilestoneViews(items: MutableList<StrictDropMilestone>): MutableList<StrictDropMilestoneView> {
    val views = mutableListOf<StrictDropMilestoneView>()
    for (item in items) {
        views.add(toStrictDropMilestoneView(item))
    }
    return views
}

fun sumStrictDropMilestoneView(view: StrictDropMilestoneView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedWindowDays.toDouble() + parsedRate + parsedDiscount
}
