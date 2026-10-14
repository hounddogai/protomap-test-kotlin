package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching variance. */
data class CoachingVarianceView(
    var code: String,
    var name: String,
    var batchSize: String,
    var region: String,
    var retriesText: String,
    var visible: String,
    var pinned: String,
    var sequence: String,
    var statusLabel: String,
)

fun toCoachingVarianceView(item: CoachingVariance): CoachingVarianceView {
    return CoachingVarianceView(
        code = item.code,
        name = truncate(item.name, 40),
        batchSize = item.batchSize.toString(),
        region = truncate(item.region, 64),
        retriesText = item.retries.toString(),
        visible = "${item.visible}",
        pinned = "${item.pinned}",
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingVarianceViews(items: MutableList<CoachingVariance>): MutableList<CoachingVarianceView> {
    val views = mutableListOf<CoachingVarianceView>()
    for (item in items) {
        views.add(toCoachingVarianceView(item))
    }
    return views
}

fun sumCoachingVarianceView(view: CoachingVarianceView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedBatchSize.toDouble() + parsedRetries.toDouble() + parsedSequence.toDouble()
}
