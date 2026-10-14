package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone override. */
data class ZoneOverrideView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var reviewerText: String,
    var distanceKmText: String,
    var expiresAt: String,
    var title: String,
    var statusLabel: String,
)

fun toZoneOverrideView(item: ZoneOverride): ZoneOverrideView {
    return ZoneOverrideView(
        code = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        reviewerText = item.reviewer,
        distanceKmText = "%.2f".format(item.distanceKm),
        expiresAt = item.expiresAt.toString(),
        title = truncate(item.title, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneOverrideViews(items: MutableList<ZoneOverride>): MutableList<ZoneOverrideView> {
    val views = mutableListOf<ZoneOverrideView>()
    for (item in items) {
        views.add(toZoneOverrideView(item))
    }
    return views
}

fun sumZoneOverrideView(view: ZoneOverrideView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedDistanceKm + parsedExpiresAt.toDouble()
}
