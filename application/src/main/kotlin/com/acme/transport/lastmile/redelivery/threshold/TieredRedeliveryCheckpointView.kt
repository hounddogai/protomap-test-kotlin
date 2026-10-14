package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered redelivery checkpoint. */
data class TieredRedeliveryCheckpointView(
    var code: String,
    var label: String,
    var description: String,
    var minimum: String,
    var categoryText: String,
    var score: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toTieredRedeliveryCheckpointView(item: TieredRedeliveryCheckpoint): TieredRedeliveryCheckpointView {
    return TieredRedeliveryCheckpointView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 80),
        description = item.description,
        minimum = "%.2f".format(item.minimum),
        categoryText = item.category,
        score = "%.2f".format(item.score),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRedeliveryCheckpointViews(items: MutableList<TieredRedeliveryCheckpoint>): MutableList<TieredRedeliveryCheckpointView> {
    val views = mutableListOf<TieredRedeliveryCheckpointView>()
    for (item in items) {
        views.add(toTieredRedeliveryCheckpointView(item))
    }
    return views
}

fun sumTieredRedeliveryCheckpointView(view: TieredRedeliveryCheckpointView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedMinimum + parsedScore
}
