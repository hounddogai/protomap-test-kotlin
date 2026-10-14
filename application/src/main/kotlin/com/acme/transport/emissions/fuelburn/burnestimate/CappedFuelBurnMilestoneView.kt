package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped fuel burn milestone. */
data class CappedFuelBurnMilestoneView(
    var code: String,
    var title: String,
    var discount: String,
    var limitText: String,
    var batchSize: String,
    var notesText: String,
    var expiresAtText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toCappedFuelBurnMilestoneView(item: CappedFuelBurnMilestone): CappedFuelBurnMilestoneView {
    return CappedFuelBurnMilestoneView(
        code = item.code,
        title = item.title,
        discount = "%.2f".format(item.discount),
        limitText = item.limit.toString(),
        batchSize = item.batchSize.toString(),
        notesText = truncate(item.notes, 80),
        expiresAtText = item.expiresAt.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedFuelBurnMilestoneViews(items: MutableList<CappedFuelBurnMilestone>): MutableList<CappedFuelBurnMilestoneView> {
    val views = mutableListOf<CappedFuelBurnMilestoneView>()
    for (item in items) {
        views.add(toCappedFuelBurnMilestoneView(item))
    }
    return views
}

fun sumCappedFuelBurnMilestoneView(view: CappedFuelBurnMilestoneView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedLimit = view.limitText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedDiscount + parsedLimit.toDouble() + parsedBatchSize.toDouble()
}
