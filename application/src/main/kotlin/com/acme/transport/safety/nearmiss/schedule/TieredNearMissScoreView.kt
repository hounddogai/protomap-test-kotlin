package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered near miss score. */
data class TieredNearMissScoreView(
    var codeText: String,
    var nameText: String,
    var notesText: String,
    var weight: String,
    var rate: String,
    var cost: String,
    var distanceKm: String,
    var budget: String,
    var pinned: String,
    var trailerId: String,
    var amountText: String,
    var statusLabel: String,
)

fun toTieredNearMissScoreView(item: TieredNearMissScore): TieredNearMissScoreView {
    return TieredNearMissScoreView(
        codeText = truncate(item.code, 120),
        nameText = item.name,
        notesText = truncate(item.notes, 80),
        weight = "%.2f".format(item.weight),
        rate = "%.2f".format(item.rate),
        cost = "%.2f".format(item.cost),
        distanceKm = "%.2f".format(item.distanceKm),
        budget = "%.2f".format(item.budget),
        pinned = "${item.pinned}",
        trailerId = truncate(item.trailerId, 64),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredNearMissScoreViews(items: MutableList<TieredNearMissScore>): MutableList<TieredNearMissScoreView> {
    val views = mutableListOf<TieredNearMissScoreView>()
    for (item in items) {
        views.add(toTieredNearMissScoreView(item))
    }
    return views
}

fun sumTieredNearMissScoreView(view: TieredNearMissScoreView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedWeight + parsedRate + parsedCost
}
