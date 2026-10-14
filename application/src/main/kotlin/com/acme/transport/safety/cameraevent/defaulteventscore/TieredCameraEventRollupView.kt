package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered camera event rollup. */
data class TieredCameraEventRollupView(
    var code: String,
    var label: String,
    var maximumText: String,
    var lane: String,
    var sequenceText: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toTieredCameraEventRollupView(item: TieredCameraEventRollup): TieredCameraEventRollupView {
    return TieredCameraEventRollupView(
        code = item.code,
        label = item.label,
        maximumText = "%.2f".format(item.maximum),
        lane = item.lane,
        sequenceText = item.sequence.toString(),
        descriptionText = truncate(item.description, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCameraEventRollupViews(items: MutableList<TieredCameraEventRollup>): MutableList<TieredCameraEventRollupView> {
    val views = mutableListOf<TieredCameraEventRollupView>()
    for (item in items) {
        views.add(toTieredCameraEventRollupView(item))
    }
    return views
}

fun sumTieredCameraEventRollupView(view: TieredCameraEventRollupView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedMaximum + parsedSequence.toDouble()
}
