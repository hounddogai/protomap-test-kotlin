package com.acme.transport.emissions.evcharge.chargescore

/** How screens and exports show a batched ev charge score. */
data class BatchedEvChargeScoreView(
    var code: String,
    var nameText: String,
    var attempts: String,
    var pinned: String,
    var dockText: String,
    var startsAt: String,
    var amount: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toBatchedEvChargeScoreView(item: BatchedEvChargeScore): BatchedEvChargeScoreView {
    return BatchedEvChargeScoreView(
        code = item.code,
        nameText = item.name,
        attempts = item.attempts.toString(),
        pinned = "${item.pinned}",
        dockText = item.dock,
        startsAt = item.startsAt.toString(),
        amount = "%.2f".format(item.amount),
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedEvChargeScoreViews(items: MutableList<BatchedEvChargeScore>): MutableList<BatchedEvChargeScoreView> {
    val views = mutableListOf<BatchedEvChargeScoreView>()
    for (item in items) {
        views.add(toBatchedEvChargeScoreView(item))
    }
    return views
}

fun sumBatchedEvChargeScoreView(view: BatchedEvChargeScoreView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedAttempts.toDouble() + parsedStartsAt.toDouble() + parsedAmount
}
