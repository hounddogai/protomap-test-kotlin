package com.acme.transport.linehaul.tender.recorditem

/** How screens and exports show a pending tender snapshot. */
data class PendingTenderSnapshotView(
    var code: String,
    var label: String,
    var threshold: String,
    var sequenceText: String,
    var score: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toPendingTenderSnapshotView(item: PendingTenderSnapshot): PendingTenderSnapshotView {
    return PendingTenderSnapshotView(
        code = item.code,
        label = item.label,
        threshold = "%.2f".format(item.threshold),
        sequenceText = item.sequence.toString(),
        score = "%.2f".format(item.score),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTenderSnapshotViews(items: MutableList<PendingTenderSnapshot>): MutableList<PendingTenderSnapshotView> {
    val views = mutableListOf<PendingTenderSnapshotView>()
    for (item in items) {
        views.add(toPendingTenderSnapshotView(item))
    }
    return views
}

fun sumPendingTenderSnapshotView(view: PendingTenderSnapshotView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedThreshold + parsedSequence.toDouble() + parsedScore
}
