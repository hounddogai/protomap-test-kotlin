package com.acme.transport.emissions.evcharge.batchedchargeslot

/** How screens and exports show a pending ev charge batch. */
data class PendingEvChargeBatchView(
    var code: String,
    var label: String,
    var threshold: String,
    var score: String,
    var minimum: String,
    var phone: String,
    var reviewerText: String,
    var ceiling: String,
    var weight: String,
    var statusLabel: String,
)

fun toPendingEvChargeBatchView(item: PendingEvChargeBatch): PendingEvChargeBatchView {
    return PendingEvChargeBatchView(
        code = item.code,
        label = item.label,
        threshold = "%.2f".format(item.threshold),
        score = "%.2f".format(item.score),
        minimum = "%.2f".format(item.minimum),
        phone = item.phone,
        reviewerText = item.reviewer,
        ceiling = "%.2f".format(item.ceiling),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingEvChargeBatchViews(items: MutableList<PendingEvChargeBatch>): MutableList<PendingEvChargeBatchView> {
    val views = mutableListOf<PendingEvChargeBatchView>()
    for (item in items) {
        views.add(toPendingEvChargeBatchView(item))
    }
    return views
}

fun sumPendingEvChargeBatchView(view: PendingEvChargeBatchView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedThreshold + parsedScore + parsedMinimum
}
