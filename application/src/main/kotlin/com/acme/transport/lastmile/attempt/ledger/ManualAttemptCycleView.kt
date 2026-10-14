package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual attempt cycle. */
data class ManualAttemptCycleView(
    var code: String,
    var name: String,
    var sequence: String,
    var updatedAtText: String,
    var pallets: String,
    var descriptionText: String,
    var scoreText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toManualAttemptCycleView(item: ManualAttemptCycle): ManualAttemptCycleView {
    return ManualAttemptCycleView(
        code = truncate(item.code, 64),
        name = item.name,
        sequence = item.sequence.toString(),
        updatedAtText = item.updatedAt.toString(),
        pallets = item.pallets.toString(),
        descriptionText = item.description,
        scoreText = "%.2f".format(item.score),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAttemptCycleViews(items: MutableList<ManualAttemptCycle>): MutableList<ManualAttemptCycleView> {
    val views = mutableListOf<ManualAttemptCycleView>()
    for (item in items) {
        views.add(toManualAttemptCycleView(item))
    }
    return views
}

fun sumManualAttemptCycleView(view: ManualAttemptCycleView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedSequence.toDouble() + parsedUpdatedAt.toDouble() + parsedPallets.toDouble()
}
