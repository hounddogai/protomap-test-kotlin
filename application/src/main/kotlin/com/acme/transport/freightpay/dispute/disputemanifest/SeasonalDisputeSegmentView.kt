package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dispute segment. */
data class SeasonalDisputeSegmentView(
    var code: String,
    var titleText: String,
    var notes: String,
    var ratio: String,
    var shiftCodeText: String,
    var stopCountText: String,
    var distanceKm: String,
    var name: String,
    var priority: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toSeasonalDisputeSegmentView(item: SeasonalDisputeSegment): SeasonalDisputeSegmentView {
    return SeasonalDisputeSegmentView(
        code = item.code,
        titleText = truncate(item.title, 120),
        notes = truncate(item.notes, 80),
        ratio = "%.2f".format(item.ratio),
        shiftCodeText = item.shiftCode,
        stopCountText = item.stopCount.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        name = item.name,
        priority = item.priority.toString(),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDisputeSegmentViews(items: MutableList<SeasonalDisputeSegment>): MutableList<SeasonalDisputeSegmentView> {
    val views = mutableListOf<SeasonalDisputeSegmentView>()
    for (item in items) {
        views.add(toSeasonalDisputeSegmentView(item))
    }
    return views
}

fun sumSeasonalDisputeSegmentView(view: SeasonalDisputeSegmentView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedRatio + parsedStopCount.toDouble() + parsedDistanceKm
}
