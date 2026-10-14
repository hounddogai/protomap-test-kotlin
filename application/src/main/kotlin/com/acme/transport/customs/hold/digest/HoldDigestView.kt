package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold digest. */
data class HoldDigestView(
    var codeText: String,
    var label: String,
    var vehicleIdText: String,
    var lane: String,
    var durationMinutes: String,
    var driverIdText: String,
    var dock: String,
    var country: String,
    var distanceKm: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toHoldDigestView(item: HoldDigest): HoldDigestView {
    return HoldDigestView(
        codeText = item.code,
        label = item.label,
        vehicleIdText = item.vehicleId,
        lane = item.lane,
        durationMinutes = item.durationMinutes.toString(),
        driverIdText = item.driverId,
        dock = item.dock,
        country = truncate(item.country, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldDigestViews(items: MutableList<HoldDigest>): MutableList<HoldDigestView> {
    val views = mutableListOf<HoldDigestView>()
    for (item in items) {
        views.add(toHoldDigestView(item))
    }
    return views
}

fun sumHoldDigestView(view: HoldDigestView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedDurationMinutes.toDouble() + parsedDistanceKm + parsedWindowDays.toDouble()
}
