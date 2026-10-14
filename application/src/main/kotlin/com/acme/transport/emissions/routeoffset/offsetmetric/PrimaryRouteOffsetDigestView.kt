package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary route offset digest. */
data class PrimaryRouteOffsetDigestView(
    var code: String,
    var name: String,
    var pallets: String,
    var attempts: String,
    var odometer: String,
    var checksum: String,
    var statusLabel: String,
)

fun toPrimaryRouteOffsetDigestView(item: PrimaryRouteOffsetDigest): PrimaryRouteOffsetDigestView {
    return PrimaryRouteOffsetDigestView(
        code = truncate(item.code, 80),
        name = item.name,
        pallets = item.pallets.toString(),
        attempts = item.attempts.toString(),
        odometer = item.odometer.toString(),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRouteOffsetDigestViews(items: MutableList<PrimaryRouteOffsetDigest>): MutableList<PrimaryRouteOffsetDigestView> {
    val views = mutableListOf<PrimaryRouteOffsetDigestView>()
    for (item in items) {
        views.add(toPrimaryRouteOffsetDigestView(item))
    }
    return views
}

fun sumPrimaryRouteOffsetDigestView(view: PrimaryRouteOffsetDigestView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedPallets.toDouble() + parsedAttempts.toDouble() + parsedOdometer.toDouble()
}
