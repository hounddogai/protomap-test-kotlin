package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire batch. */
data class TireBatchView(
    var codeText: String,
    var title: String,
    var sequenceText: String,
    var scoreText: String,
    var discountText: String,
    var archived: String,
    var statusLabel: String,
)

fun toTireBatchView(item: TireBatch): TireBatchView {
    return TireBatchView(
        codeText = truncate(item.code, 64),
        title = truncate(item.title, 80),
        sequenceText = item.sequence.toString(),
        scoreText = "%.2f".format(item.score),
        discountText = "%.2f".format(item.discount),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireBatchViews(items: MutableList<TireBatch>): MutableList<TireBatchView> {
    val views = mutableListOf<TireBatchView>()
    for (item in items) {
        views.add(toTireBatchView(item))
    }
    return views
}

fun sumTireBatchView(view: TireBatchView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedSequence.toDouble() + parsedScore + parsedDiscount
}
