package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback swap manifest. */
data class FallbackSwapManifestView(
    var code: String,
    var title: String,
    var trailerId: String,
    var updatedAt: String,
    var currency: String,
    var contactEmail: String,
    var regionText: String,
    var minimum: String,
    var driverId: String,
    var windowDaysText: String,
    var approved: String,
    var statusLabel: String,
)

fun toFallbackSwapManifestView(item: FallbackSwapManifest): FallbackSwapManifestView {
    return FallbackSwapManifestView(
        code = truncate(item.code, 120),
        title = item.title,
        trailerId = truncate(item.trailerId, 64),
        updatedAt = item.updatedAt.toString(),
        currency = item.currency,
        contactEmail = truncate(item.contactEmail, 80),
        regionText = truncate(item.region, 64),
        minimum = "%.2f".format(item.minimum),
        driverId = item.driverId,
        windowDaysText = item.windowDays.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackSwapManifestViews(items: MutableList<FallbackSwapManifest>): MutableList<FallbackSwapManifestView> {
    val views = mutableListOf<FallbackSwapManifestView>()
    for (item in items) {
        views.add(toFallbackSwapManifestView(item))
    }
    return views
}

fun sumFallbackSwapManifestView(view: FallbackSwapManifestView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedUpdatedAt.toDouble() + parsedMinimum + parsedWindowDays.toDouble()
}
