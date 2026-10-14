package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered clearance draft. */
data class TieredClearanceDraftView(
    var codeText: String,
    var title: String,
    var odometer: String,
    var discount: String,
    var expiresAt: String,
    var ownerId: String,
    var lane: String,
    var timezone: String,
    var segmentText: String,
    var checksum: String,
    var statusLabel: String,
)

fun toTieredClearanceDraftView(item: TieredClearanceDraft): TieredClearanceDraftView {
    return TieredClearanceDraftView(
        codeText = item.code,
        title = item.title,
        odometer = item.odometer.toString(),
        discount = "%.2f".format(item.discount),
        expiresAt = item.expiresAt.toString(),
        ownerId = item.ownerId,
        lane = truncate(item.lane, 40),
        timezone = item.timezone,
        segmentText = item.segment,
        checksum = truncate(item.checksum, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredClearanceDraftViews(items: MutableList<TieredClearanceDraft>): MutableList<TieredClearanceDraftView> {
    val views = mutableListOf<TieredClearanceDraftView>()
    for (item in items) {
        views.add(toTieredClearanceDraftView(item))
    }
    return views
}

fun sumTieredClearanceDraftView(view: TieredClearanceDraftView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedOdometer.toDouble() + parsedDiscount + parsedExpiresAt.toDouble()
}
