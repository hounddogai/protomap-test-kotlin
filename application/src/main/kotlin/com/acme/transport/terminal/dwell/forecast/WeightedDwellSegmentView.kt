package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted dwell segment. */
data class WeightedDwellSegmentView(
    var codeText: String,
    var name: String,
    var checksum: String,
    var region: String,
    var carrier: String,
    var externalRef: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toWeightedDwellSegmentView(item: WeightedDwellSegment): WeightedDwellSegmentView {
    return WeightedDwellSegmentView(
        codeText = item.code,
        name = item.name,
        checksum = item.checksum,
        region = truncate(item.region, 80),
        carrier = item.carrier,
        externalRef = item.externalRef,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDwellSegmentViews(items: MutableList<WeightedDwellSegment>): MutableList<WeightedDwellSegmentView> {
    val views = mutableListOf<WeightedDwellSegmentView>()
    for (item in items) {
        views.add(toWeightedDwellSegmentView(item))
    }
    return views
}

fun sumWeightedDwellSegmentView(view: WeightedDwellSegmentView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
