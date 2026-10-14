package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled transit time digest. */
data class ScheduledTransitTimeDigestView(
    var code: String,
    var titleText: String,
    var stopCount: String,
    var budget: String,
    var visible: String,
    var checksum: String,
    var categoryText: String,
    var discount: String,
    var statusLabel: String,
)

fun toScheduledTransitTimeDigestView(item: ScheduledTransitTimeDigest): ScheduledTransitTimeDigestView {
    return ScheduledTransitTimeDigestView(
        code = item.code,
        titleText = item.title,
        stopCount = item.stopCount.toString(),
        budget = "%.2f".format(item.budget),
        visible = "${item.visible}",
        checksum = item.checksum,
        categoryText = truncate(item.category, 40),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTransitTimeDigestViews(items: MutableList<ScheduledTransitTimeDigest>): MutableList<ScheduledTransitTimeDigestView> {
    val views = mutableListOf<ScheduledTransitTimeDigestView>()
    for (item in items) {
        views.add(toScheduledTransitTimeDigestView(item))
    }
    return views
}

fun sumScheduledTransitTimeDigestView(view: ScheduledTransitTimeDigestView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedStopCount.toDouble() + parsedBudget + parsedDiscount
}
