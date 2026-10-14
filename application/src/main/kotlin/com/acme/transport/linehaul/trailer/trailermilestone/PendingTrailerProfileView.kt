package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending trailer profile. */
data class PendingTrailerProfileView(
    var code: String,
    var name: String,
    var routeCode: String,
    var threshold: String,
    var checksumText: String,
    var createdAtText: String,
    var driverId: String,
    var phone: String,
    var lane: String,
    var expiresAt: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toPendingTrailerProfileView(item: PendingTrailerProfile): PendingTrailerProfileView {
    return PendingTrailerProfileView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 40),
        routeCode = truncate(item.routeCode, 64),
        threshold = "%.2f".format(item.threshold),
        checksumText = item.checksum,
        createdAtText = item.createdAt.toString(),
        driverId = item.driverId,
        phone = item.phone,
        lane = item.lane,
        expiresAt = item.expiresAt.toString(),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTrailerProfileViews(items: MutableList<PendingTrailerProfile>): MutableList<PendingTrailerProfileView> {
    val views = mutableListOf<PendingTrailerProfileView>()
    for (item in items) {
        views.add(toPendingTrailerProfileView(item))
    }
    return views
}

fun sumPendingTrailerProfileView(view: PendingTrailerProfileView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedThreshold + parsedCreatedAt.toDouble() + parsedExpiresAt.toDouble()
}
