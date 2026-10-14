package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone review. */
data class ZoneReviewView(
    var code: String,
    var name: String,
    var odometer: String,
    var description: String,
    var windowDaysText: String,
    var country: String,
    var source: String,
    var carrierText: String,
    var minimumText: String,
    var retries: String,
    var statusLabel: String,
)

fun toZoneReviewView(item: ZoneReview): ZoneReviewView {
    return ZoneReviewView(
        code = item.code,
        name = truncate(item.name, 64),
        odometer = item.odometer.toString(),
        description = item.description,
        windowDaysText = item.windowDays.toString(),
        country = truncate(item.country, 120),
        source = item.source,
        carrierText = truncate(item.carrier, 40),
        minimumText = "%.2f".format(item.minimum),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneReviewViews(items: MutableList<ZoneReview>): MutableList<ZoneReviewView> {
    val views = mutableListOf<ZoneReviewView>()
    for (item in items) {
        views.add(toZoneReviewView(item))
    }
    return views
}

fun sumZoneReviewView(view: ZoneReviewView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedOdometer.toDouble() + parsedWindowDays.toDouble() + parsedMinimum
}
