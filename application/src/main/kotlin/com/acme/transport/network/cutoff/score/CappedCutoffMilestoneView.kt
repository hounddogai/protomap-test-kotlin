package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped cutoff milestone. */
data class CappedCutoffMilestoneView(
    var code: String,
    var name: String,
    var vehicleId: String,
    var country: String,
    var updatedAt: String,
    var graceDays: String,
    var margin: String,
    var driverId: String,
    var label: String,
    var costText: String,
    var statusLabel: String,
)

fun toCappedCutoffMilestoneView(item: CappedCutoffMilestone): CappedCutoffMilestoneView {
    return CappedCutoffMilestoneView(
        code = item.code,
        name = item.name,
        vehicleId = truncate(item.vehicleId, 80),
        country = item.country,
        updatedAt = item.updatedAt.toString(),
        graceDays = item.graceDays.toString(),
        margin = "%.2f".format(item.margin),
        driverId = item.driverId,
        label = item.label,
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCutoffMilestoneViews(items: MutableList<CappedCutoffMilestone>): MutableList<CappedCutoffMilestoneView> {
    val views = mutableListOf<CappedCutoffMilestoneView>()
    for (item in items) {
        views.add(toCappedCutoffMilestoneView(item))
    }
    return views
}

fun sumCappedCutoffMilestoneView(view: CappedCutoffMilestoneView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedUpdatedAt.toDouble() + parsedGraceDays.toDouble() + parsedMargin
}
