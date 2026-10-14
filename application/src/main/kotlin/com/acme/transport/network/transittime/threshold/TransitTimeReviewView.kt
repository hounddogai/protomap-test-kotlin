package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time review. */
data class TransitTimeReviewView(
    var code: String,
    var label: String,
    var checksum: String,
    var name: String,
    var unit: String,
    var marginText: String,
    var ratio: String,
    var palletsText: String,
    var fuelLiters: String,
    var carrier: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toTransitTimeReviewView(item: TransitTimeReview): TransitTimeReviewView {
    return TransitTimeReviewView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 40),
        checksum = truncate(item.checksum, 64),
        name = item.name,
        unit = item.unit,
        marginText = "%.2f".format(item.margin),
        ratio = "%.2f".format(item.ratio),
        palletsText = item.pallets.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        carrier = truncate(item.carrier, 120),
        driverIdText = truncate(item.driverId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeReviewViews(items: MutableList<TransitTimeReview>): MutableList<TransitTimeReviewView> {
    val views = mutableListOf<TransitTimeReviewView>()
    for (item in items) {
        views.add(toTransitTimeReviewView(item))
    }
    return views
}

fun sumTransitTimeReviewView(view: TransitTimeReviewView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedMargin + parsedRatio + parsedPallets.toDouble()
}
