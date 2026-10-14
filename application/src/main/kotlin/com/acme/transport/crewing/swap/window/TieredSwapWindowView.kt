package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered swap window. */
data class TieredSwapWindowView(
    var code: String,
    var label: String,
    var versionText: String,
    var durationMinutes: String,
    var stopCount: String,
    var discount: String,
    var trailerId: String,
    var threshold: String,
    var externalRef: String,
    var checksum: String,
    var statusLabel: String,
)

fun toTieredSwapWindowView(item: TieredSwapWindow): TieredSwapWindowView {
    return TieredSwapWindowView(
        code = item.code,
        label = item.label,
        versionText = item.version.toString(),
        durationMinutes = item.durationMinutes.toString(),
        stopCount = item.stopCount.toString(),
        discount = "%.2f".format(item.discount),
        trailerId = truncate(item.trailerId, 80),
        threshold = "%.2f".format(item.threshold),
        externalRef = truncate(item.externalRef, 120),
        checksum = truncate(item.checksum, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSwapWindowViews(items: MutableList<TieredSwapWindow>): MutableList<TieredSwapWindowView> {
    val views = mutableListOf<TieredSwapWindowView>()
    for (item in items) {
        views.add(toTieredSwapWindowView(item))
    }
    return views
}

fun sumTieredSwapWindowView(view: TieredSwapWindowView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedVersion.toDouble() + parsedDurationMinutes.toDouble() + parsedStopCount.toDouble()
}
