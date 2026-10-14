package com.acme.transport.linehaul.relay.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay digest. */
data class RelayDigestView(
    var code: String,
    var name: String,
    var archived: String,
    var version: String,
    var windowDaysText: String,
    var odometerText: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toRelayDigestView(item: RelayDigest): RelayDigestView {
    return RelayDigestView(
        code = truncate(item.code, 64),
        name = item.name,
        archived = "${item.archived}",
        version = item.version.toString(),
        windowDaysText = item.windowDays.toString(),
        odometerText = item.odometer.toString(),
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayDigestViews(items: MutableList<RelayDigest>): MutableList<RelayDigestView> {
    val views = mutableListOf<RelayDigestView>()
    for (item in items) {
        views.add(toRelayDigestView(item))
    }
    return views
}

fun sumRelayDigestView(view: RelayDigestView): Double {
    val parsedVersion = view.version.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedVersion.toDouble() + parsedWindowDays.toDouble() + parsedOdometer.toDouble()
}
