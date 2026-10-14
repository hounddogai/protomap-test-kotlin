package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery target. */
data class ProofOfDeliveryTargetView(
    var code: String,
    var labelText: String,
    var depot: String,
    var retries: String,
    var attempts: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryTargetView(item: ProofOfDeliveryTarget): ProofOfDeliveryTargetView {
    return ProofOfDeliveryTargetView(
        code = truncate(item.code, 120),
        labelText = truncate(item.label, 120),
        depot = truncate(item.depot, 120),
        retries = item.retries.toString(),
        attempts = item.attempts.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryTargetViews(items: MutableList<ProofOfDeliveryTarget>): MutableList<ProofOfDeliveryTargetView> {
    val views = mutableListOf<ProofOfDeliveryTargetView>()
    for (item in items) {
        views.add(toProofOfDeliveryTargetView(item))
    }
    return views
}

fun sumProofOfDeliveryTargetView(view: ProofOfDeliveryTargetView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedRetries.toDouble() + parsedAttempts.toDouble() + parsedDurationMinutes.toDouble()
}
