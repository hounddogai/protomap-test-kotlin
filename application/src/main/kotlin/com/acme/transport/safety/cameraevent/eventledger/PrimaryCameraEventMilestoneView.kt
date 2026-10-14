package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary camera event milestone. */
data class PrimaryCameraEventMilestoneView(
    var code: String,
    var titleText: String,
    var limit: String,
    var costText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toPrimaryCameraEventMilestoneView(item: PrimaryCameraEventMilestone): PrimaryCameraEventMilestoneView {
    return PrimaryCameraEventMilestoneView(
        code = truncate(item.code, 120),
        titleText = item.title,
        limit = item.limit.toString(),
        costText = "%.2f".format(item.cost),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCameraEventMilestoneViews(items: MutableList<PrimaryCameraEventMilestone>): MutableList<PrimaryCameraEventMilestoneView> {
    val views = mutableListOf<PrimaryCameraEventMilestoneView>()
    for (item in items) {
        views.add(toPrimaryCameraEventMilestoneView(item))
    }
    return views
}

fun sumPrimaryCameraEventMilestoneView(view: PrimaryCameraEventMilestoneView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedLimit.toDouble() + parsedCost + parsedDurationMinutes.toDouble()
}
