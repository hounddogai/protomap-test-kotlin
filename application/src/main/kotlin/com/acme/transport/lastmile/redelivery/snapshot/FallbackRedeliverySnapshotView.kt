package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback redelivery snapshot. */
data class FallbackRedeliverySnapshotView(
    var code: String,
    var name: String,
    var weightText: String,
    var externalRef: String,
    var sequenceText: String,
    var summary: String,
    var contactEmail: String,
    var threshold: String,
    var depotText: String,
    var durationMinutes: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toFallbackRedeliverySnapshotView(item: FallbackRedeliverySnapshot): FallbackRedeliverySnapshotView {
    return FallbackRedeliverySnapshotView(
        code = item.code,
        name = item.name,
        weightText = "%.2f".format(item.weight),
        externalRef = truncate(item.externalRef, 120),
        sequenceText = item.sequence.toString(),
        summary = item.summary,
        contactEmail = item.contactEmail,
        threshold = "%.2f".format(item.threshold),
        depotText = item.depot,
        durationMinutes = item.durationMinutes.toString(),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRedeliverySnapshotViews(items: MutableList<FallbackRedeliverySnapshot>): MutableList<FallbackRedeliverySnapshotView> {
    val views = mutableListOf<FallbackRedeliverySnapshotView>()
    for (item in items) {
        views.add(toFallbackRedeliverySnapshotView(item))
    }
    return views
}

fun sumFallbackRedeliverySnapshotView(view: FallbackRedeliverySnapshotView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedWeight + parsedSequence.toDouble() + parsedThreshold
}
