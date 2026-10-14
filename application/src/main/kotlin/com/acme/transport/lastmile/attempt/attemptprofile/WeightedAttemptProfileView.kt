package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted attempt profile. */
data class WeightedAttemptProfileView(
    var codeText: String,
    var title: String,
    var startsAt: String,
    var amountText: String,
    var carrier: String,
    var batchSize: String,
    var sequence: String,
    var quantity: String,
    var externalRef: String,
    var dock: String,
    var segment: String,
    var statusLabel: String,
)

fun toWeightedAttemptProfileView(item: WeightedAttemptProfile): WeightedAttemptProfileView {
    return WeightedAttemptProfileView(
        codeText = item.code,
        title = item.title,
        startsAt = item.startsAt.toString(),
        amountText = "%.2f".format(item.amount),
        carrier = truncate(item.carrier, 80),
        batchSize = item.batchSize.toString(),
        sequence = item.sequence.toString(),
        quantity = item.quantity.toString(),
        externalRef = item.externalRef,
        dock = truncate(item.dock, 120),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAttemptProfileViews(items: MutableList<WeightedAttemptProfile>): MutableList<WeightedAttemptProfileView> {
    val views = mutableListOf<WeightedAttemptProfileView>()
    for (item in items) {
        views.add(toWeightedAttemptProfileView(item))
    }
    return views
}

fun sumWeightedAttemptProfileView(view: WeightedAttemptProfileView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedStartsAt.toDouble() + parsedAmount + parsedBatchSize.toDouble()
}
