package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn milestone. */
data class FuelBurnMilestoneView(
    var code: String,
    var title: String,
    var phone: String,
    var routeCodeText: String,
    var activeText: String,
    var sequenceText: String,
    var ratioText: String,
    var updatedAt: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toFuelBurnMilestoneView(item: FuelBurnMilestone): FuelBurnMilestoneView {
    return FuelBurnMilestoneView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 64),
        phone = item.phone,
        routeCodeText = truncate(item.routeCode, 80),
        activeText = "${item.active}",
        sequenceText = item.sequence.toString(),
        ratioText = "%.2f".format(item.ratio),
        updatedAt = item.updatedAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnMilestoneViews(items: MutableList<FuelBurnMilestone>): MutableList<FuelBurnMilestoneView> {
    val views = mutableListOf<FuelBurnMilestoneView>()
    for (item in items) {
        views.add(toFuelBurnMilestoneView(item))
    }
    return views
}

fun sumFuelBurnMilestoneView(view: FuelBurnMilestoneView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedSequence.toDouble() + parsedRatio + parsedUpdatedAt.toDouble()
}
