package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted rest break score. */
data class WeightedRestBreakScoreView(
    var code: String,
    var title: String,
    var summary: String,
    var approved: String,
    var updatedAt: String,
    var score: String,
    var statusLabel: String,
)

fun toWeightedRestBreakScoreView(item: WeightedRestBreakScore): WeightedRestBreakScoreView {
    return WeightedRestBreakScoreView(
        code = item.code,
        title = item.title,
        summary = truncate(item.summary, 64),
        approved = "${item.approved}",
        updatedAt = item.updatedAt.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRestBreakScoreViews(items: MutableList<WeightedRestBreakScore>): MutableList<WeightedRestBreakScoreView> {
    val views = mutableListOf<WeightedRestBreakScoreView>()
    for (item in items) {
        views.add(toWeightedRestBreakScoreView(item))
    }
    return views
}

fun sumWeightedRestBreakScoreView(view: WeightedRestBreakScoreView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedScore = view.score.toDouble()
    return parsedUpdatedAt.toDouble() + parsedScore
}
