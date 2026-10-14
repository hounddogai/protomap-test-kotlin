package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted scorecard review. */
data class WeightedScorecardReviewView(
    var code: String,
    var name: String,
    var ratio: String,
    var batchSize: String,
    var carrier: String,
    var notesText: String,
    var visible: String,
    var pinned: String,
    var titleText: String,
    var statusLabel: String,
)

fun toWeightedScorecardReviewView(item: WeightedScorecardReview): WeightedScorecardReviewView {
    return WeightedScorecardReviewView(
        code = item.code,
        name = truncate(item.name, 120),
        ratio = "%.2f".format(item.ratio),
        batchSize = item.batchSize.toString(),
        carrier = item.carrier,
        notesText = item.notes,
        visible = "${item.visible}",
        pinned = "${item.pinned}",
        titleText = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedScorecardReviewViews(items: MutableList<WeightedScorecardReview>): MutableList<WeightedScorecardReviewView> {
    val views = mutableListOf<WeightedScorecardReviewView>()
    for (item in items) {
        views.add(toWeightedScorecardReviewView(item))
    }
    return views
}

fun sumWeightedScorecardReviewView(view: WeightedScorecardReviewView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedRatio + parsedBatchSize.toDouble()
}
