package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched attempt tier. */
data class BatchedAttemptTierView(
    var code: String,
    var labelText: String,
    var endsAtText: String,
    var archived: String,
    var fuelLiters: String,
    var costText: String,
    var trailerIdText: String,
    var source: String,
    var statusLabel: String,
)

fun toBatchedAttemptTierView(item: BatchedAttemptTier): BatchedAttemptTierView {
    return BatchedAttemptTierView(
        code = truncate(item.code, 40),
        labelText = item.label,
        endsAtText = item.endsAt.toString(),
        archived = "${item.archived}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        costText = "%.2f".format(item.cost),
        trailerIdText = truncate(item.trailerId, 40),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAttemptTierViews(items: MutableList<BatchedAttemptTier>): MutableList<BatchedAttemptTierView> {
    val views = mutableListOf<BatchedAttemptTierView>()
    for (item in items) {
        views.add(toBatchedAttemptTierView(item))
    }
    return views
}

fun sumBatchedAttemptTierView(view: BatchedAttemptTierView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedEndsAt.toDouble() + parsedFuelLiters + parsedCost
}
