package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery adjustment. */
data class ProofOfDeliveryAdjustmentView(
    var code: String,
    var title: String,
    var pinnedText: String,
    var priorityText: String,
    var attemptsText: String,
    var windowDays: String,
    var description: String,
    var statusLabel: String,
)

fun toProofOfDeliveryAdjustmentView(item: ProofOfDeliveryAdjustment): ProofOfDeliveryAdjustmentView {
    return ProofOfDeliveryAdjustmentView(
        code = item.code,
        title = truncate(item.title, 40),
        pinnedText = "${item.pinned}",
        priorityText = item.priority.toString(),
        attemptsText = item.attempts.toString(),
        windowDays = item.windowDays.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryAdjustmentViews(items: MutableList<ProofOfDeliveryAdjustment>): MutableList<ProofOfDeliveryAdjustmentView> {
    val views = mutableListOf<ProofOfDeliveryAdjustmentView>()
    for (item in items) {
        views.add(toProofOfDeliveryAdjustmentView(item))
    }
    return views
}

fun sumProofOfDeliveryAdjustmentView(view: ProofOfDeliveryAdjustmentView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedPriority.toDouble() + parsedAttempts.toDouble() + parsedWindowDays.toDouble()
}
