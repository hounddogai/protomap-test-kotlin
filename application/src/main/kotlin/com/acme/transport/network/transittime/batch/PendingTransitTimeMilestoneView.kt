package com.acme.transport.network.transittime.batch

/** How screens and exports show a pending transit time milestone. */
data class PendingTransitTimeMilestoneView(
    var code: String,
    var nameText: String,
    var carrier: String,
    var quantity: String,
    var channel: String,
    var maximum: String,
    var statusLabel: String,
)

fun toPendingTransitTimeMilestoneView(item: PendingTransitTimeMilestone): PendingTransitTimeMilestoneView {
    return PendingTransitTimeMilestoneView(
        code = item.code,
        nameText = item.name,
        carrier = item.carrier,
        quantity = item.quantity.toString(),
        channel = item.channel,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTransitTimeMilestoneViews(items: MutableList<PendingTransitTimeMilestone>): MutableList<PendingTransitTimeMilestoneView> {
    val views = mutableListOf<PendingTransitTimeMilestoneView>()
    for (item in items) {
        views.add(toPendingTransitTimeMilestoneView(item))
    }
    return views
}

fun sumPendingTransitTimeMilestoneView(view: PendingTransitTimeMilestoneView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedQuantity.toDouble() + parsedMaximum
}
