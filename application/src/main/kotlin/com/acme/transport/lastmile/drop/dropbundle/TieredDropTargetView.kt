package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered drop target. */
data class TieredDropTargetView(
    var code: String,
    var title: String,
    var segmentText: String,
    var expiresAtText: String,
    var notes: String,
    var durationMinutesText: String,
    var checksumText: String,
    var currency: String,
    var statusLabel: String,
)

fun toTieredDropTargetView(item: TieredDropTarget): TieredDropTargetView {
    return TieredDropTargetView(
        code = item.code,
        title = truncate(item.title, 40),
        segmentText = truncate(item.segment, 64),
        expiresAtText = item.expiresAt.toString(),
        notes = item.notes,
        durationMinutesText = item.durationMinutes.toString(),
        checksumText = item.checksum,
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDropTargetViews(items: MutableList<TieredDropTarget>): MutableList<TieredDropTargetView> {
    val views = mutableListOf<TieredDropTargetView>()
    for (item in items) {
        views.add(toTieredDropTargetView(item))
    }
    return views
}

fun sumTieredDropTargetView(view: TieredDropTargetView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedExpiresAt.toDouble() + parsedDurationMinutes.toDouble()
}
