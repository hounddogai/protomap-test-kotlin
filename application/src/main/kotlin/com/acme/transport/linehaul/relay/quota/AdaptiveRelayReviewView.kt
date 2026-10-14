package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive relay review. */
data class AdaptiveRelayReviewView(
    var code: String,
    var label: String,
    var carrierText: String,
    var startsAt: String,
    var checksumText: String,
    var depot: String,
    var shiftCodeText: String,
    var versionText: String,
    var distanceKm: String,
    var routeCode: String,
    var region: String,
    var statusLabel: String,
)

fun toAdaptiveRelayReviewView(item: AdaptiveRelayReview): AdaptiveRelayReviewView {
    return AdaptiveRelayReviewView(
        code = item.code,
        label = item.label,
        carrierText = truncate(item.carrier, 64),
        startsAt = item.startsAt.toString(),
        checksumText = truncate(item.checksum, 80),
        depot = truncate(item.depot, 80),
        shiftCodeText = item.shiftCode,
        versionText = item.version.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        routeCode = truncate(item.routeCode, 80),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRelayReviewViews(items: MutableList<AdaptiveRelayReview>): MutableList<AdaptiveRelayReviewView> {
    val views = mutableListOf<AdaptiveRelayReviewView>()
    for (item in items) {
        views.add(toAdaptiveRelayReviewView(item))
    }
    return views
}

fun sumAdaptiveRelayReviewView(view: AdaptiveRelayReviewView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedStartsAt.toDouble() + parsedVersion.toDouble() + parsedDistanceKm
}
