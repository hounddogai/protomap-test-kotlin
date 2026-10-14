package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted hold segment. */
data class WeightedHoldSegmentView(
    var code: String,
    var title: String,
    var batchSize: String,
    var lane: String,
    var stopCountText: String,
    var cost: String,
    var durationMinutesText: String,
    var etaMinutesText: String,
    var phone: String,
    var locale: String,
    var archived: String,
    var statusLabel: String,
)

fun toWeightedHoldSegmentView(item: WeightedHoldSegment): WeightedHoldSegmentView {
    return WeightedHoldSegmentView(
        code = item.code,
        title = item.title,
        batchSize = item.batchSize.toString(),
        lane = item.lane,
        stopCountText = item.stopCount.toString(),
        cost = "%.2f".format(item.cost),
        durationMinutesText = item.durationMinutes.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        phone = truncate(item.phone, 64),
        locale = item.locale,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedHoldSegmentViews(items: MutableList<WeightedHoldSegment>): MutableList<WeightedHoldSegmentView> {
    val views = mutableListOf<WeightedHoldSegmentView>()
    for (item in items) {
        views.add(toWeightedHoldSegmentView(item))
    }
    return views
}

fun sumWeightedHoldSegmentView(view: WeightedHoldSegmentView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedBatchSize.toDouble() + parsedStopCount.toDouble() + parsedCost
}
