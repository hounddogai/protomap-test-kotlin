package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default redelivery milestone. */
data class DefaultRedeliveryMilestoneView(
    var code: String,
    var label: String,
    var depotText: String,
    var palletsText: String,
    var windowDaysText: String,
    var visibleText: String,
    var margin: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toDefaultRedeliveryMilestoneView(item: DefaultRedeliveryMilestone): DefaultRedeliveryMilestoneView {
    return DefaultRedeliveryMilestoneView(
        code = truncate(item.code, 40),
        label = item.label,
        depotText = truncate(item.depot, 64),
        palletsText = item.pallets.toString(),
        windowDaysText = item.windowDays.toString(),
        visibleText = "${item.visible}",
        margin = "%.2f".format(item.margin),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRedeliveryMilestoneViews(items: MutableList<DefaultRedeliveryMilestone>): MutableList<DefaultRedeliveryMilestoneView> {
    val views = mutableListOf<DefaultRedeliveryMilestoneView>()
    for (item in items) {
        views.add(toDefaultRedeliveryMilestoneView(item))
    }
    return views
}

fun sumDefaultRedeliveryMilestoneView(view: DefaultRedeliveryMilestoneView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedPallets.toDouble() + parsedWindowDays.toDouble() + parsedMargin
}
