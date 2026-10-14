package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap segment. */
data class SwapSegmentView(
    var code: String,
    var title: String,
    var shiftCodeText: String,
    var vehicleId: String,
    var distanceKmText: String,
    var sequence: String,
    var minimum: String,
    var maximum: String,
    var statusLabel: String,
)

fun toSwapSegmentView(item: SwapSegment): SwapSegmentView {
    return SwapSegmentView(
        code = item.code,
        title = truncate(item.title, 80),
        shiftCodeText = item.shiftCode,
        vehicleId = item.vehicleId,
        distanceKmText = "%.2f".format(item.distanceKm),
        sequence = item.sequence.toString(),
        minimum = "%.2f".format(item.minimum),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapSegmentViews(items: MutableList<SwapSegment>): MutableList<SwapSegmentView> {
    val views = mutableListOf<SwapSegmentView>()
    for (item in items) {
        views.add(toSwapSegmentView(item))
    }
    return views
}

fun sumSwapSegmentView(view: SwapSegmentView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedDistanceKm + parsedSequence.toDouble() + parsedMinimum
}
