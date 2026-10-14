package com.acme.transport.lastmile.attempt.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary attempt score. */
data class PrimaryAttemptScoreView(
    var codeText: String,
    var name: String,
    var ceilingText: String,
    var distanceKmText: String,
    var batchSizeText: String,
    var updatedAtText: String,
    var archived: String,
    var capacityText: String,
    var lane: String,
    var country: String,
    var statusLabel: String,
)

fun toPrimaryAttemptScoreView(item: PrimaryAttemptScore): PrimaryAttemptScoreView {
    return PrimaryAttemptScoreView(
        codeText = item.code,
        name = item.name,
        ceilingText = "%.2f".format(item.ceiling),
        distanceKmText = "%.2f".format(item.distanceKm),
        batchSizeText = item.batchSize.toString(),
        updatedAtText = item.updatedAt.toString(),
        archived = "${item.archived}",
        capacityText = item.capacity.toString(),
        lane = item.lane,
        country = truncate(item.country, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAttemptScoreViews(items: MutableList<PrimaryAttemptScore>): MutableList<PrimaryAttemptScoreView> {
    val views = mutableListOf<PrimaryAttemptScoreView>()
    for (item in items) {
        views.add(toPrimaryAttemptScoreView(item))
    }
    return views
}

fun sumPrimaryAttemptScoreView(view: PrimaryAttemptScoreView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedCeiling + parsedDistanceKm + parsedBatchSize.toDouble()
}
