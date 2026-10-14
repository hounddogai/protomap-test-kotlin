package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap checkpoint. */
data class SwapCheckpointView(
    var code: String,
    var name: String,
    var maximum: String,
    var timezone: String,
    var thresholdText: String,
    var margin: String,
    var localeText: String,
    var statusLabel: String,
)

fun toSwapCheckpointView(item: SwapCheckpoint): SwapCheckpointView {
    return SwapCheckpointView(
        code = item.code,
        name = truncate(item.name, 80),
        maximum = "%.2f".format(item.maximum),
        timezone = item.timezone,
        thresholdText = "%.2f".format(item.threshold),
        margin = "%.2f".format(item.margin),
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapCheckpointViews(items: MutableList<SwapCheckpoint>): MutableList<SwapCheckpointView> {
    val views = mutableListOf<SwapCheckpointView>()
    for (item in items) {
        views.add(toSwapCheckpointView(item))
    }
    return views
}

fun sumSwapCheckpointView(view: SwapCheckpointView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedMaximum + parsedThreshold + parsedMargin
}
