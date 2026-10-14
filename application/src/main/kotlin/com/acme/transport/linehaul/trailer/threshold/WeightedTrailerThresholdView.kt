package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted trailer threshold. */
data class WeightedTrailerThresholdView(
    var code: String,
    var name: String,
    var ratioText: String,
    var expiresAt: String,
    var ownerId: String,
    var score: String,
    var maximum: String,
    var priority: String,
    var label: String,
    var marginText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toWeightedTrailerThresholdView(item: WeightedTrailerThreshold): WeightedTrailerThresholdView {
    return WeightedTrailerThresholdView(
        code = item.code,
        name = item.name,
        ratioText = "%.2f".format(item.ratio),
        expiresAt = item.expiresAt.toString(),
        ownerId = truncate(item.ownerId, 40),
        score = "%.2f".format(item.score),
        maximum = "%.2f".format(item.maximum),
        priority = item.priority.toString(),
        label = item.label,
        marginText = "%.2f".format(item.margin),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTrailerThresholdViews(items: MutableList<WeightedTrailerThreshold>): MutableList<WeightedTrailerThresholdView> {
    val views = mutableListOf<WeightedTrailerThresholdView>()
    for (item in items) {
        views.add(toWeightedTrailerThresholdView(item))
    }
    return views
}

fun sumWeightedTrailerThresholdView(view: WeightedTrailerThresholdView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedScore = view.score.toDouble()
    return parsedRatio + parsedExpiresAt.toDouble() + parsedScore
}
