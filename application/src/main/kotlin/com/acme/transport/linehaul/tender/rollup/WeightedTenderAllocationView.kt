package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted tender allocation. */
data class WeightedTenderAllocationView(
    var codeText: String,
    var labelText: String,
    var sequence: String,
    var endsAtText: String,
    var attempts: String,
    var score: String,
    var statusLabel: String,
)

fun toWeightedTenderAllocationView(item: WeightedTenderAllocation): WeightedTenderAllocationView {
    return WeightedTenderAllocationView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        sequence = item.sequence.toString(),
        endsAtText = item.endsAt.toString(),
        attempts = item.attempts.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTenderAllocationViews(items: MutableList<WeightedTenderAllocation>): MutableList<WeightedTenderAllocationView> {
    val views = mutableListOf<WeightedTenderAllocationView>()
    for (item in items) {
        views.add(toWeightedTenderAllocationView(item))
    }
    return views
}

fun sumWeightedTenderAllocationView(view: WeightedTenderAllocationView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedSequence.toDouble() + parsedEndsAt.toDouble() + parsedAttempts.toDouble()
}
