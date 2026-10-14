package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict swap checkpoint. */
data class StrictSwapCheckpointView(
    var code: String,
    var title: String,
    var windowDays: String,
    var pallets: String,
    var timezoneText: String,
    var regionText: String,
    var expiresAt: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toStrictSwapCheckpointView(item: StrictSwapCheckpoint): StrictSwapCheckpointView {
    return StrictSwapCheckpointView(
        code = truncate(item.code, 64),
        title = item.title,
        windowDays = item.windowDays.toString(),
        pallets = item.pallets.toString(),
        timezoneText = truncate(item.timezone, 80),
        regionText = item.region,
        expiresAt = item.expiresAt.toString(),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictSwapCheckpointViews(items: MutableList<StrictSwapCheckpoint>): MutableList<StrictSwapCheckpointView> {
    val views = mutableListOf<StrictSwapCheckpointView>()
    for (item in items) {
        views.add(toStrictSwapCheckpointView(item))
    }
    return views
}

fun sumStrictSwapCheckpointView(view: StrictSwapCheckpointView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedWindowDays.toDouble() + parsedPallets.toDouble() + parsedExpiresAt.toDouble()
}
