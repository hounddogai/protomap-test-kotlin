package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer profile. */
data class TrailerProfileView(
    var code: String,
    var name: String,
    var stopCountText: String,
    var marginText: String,
    var graceDays: String,
    var cost: String,
    var summary: String,
    var phone: String,
    var dock: String,
    var countryText: String,
    var statusLabel: String,
)

fun toTrailerProfileView(item: TrailerProfile): TrailerProfileView {
    return TrailerProfileView(
        code = truncate(item.code, 40),
        name = item.name,
        stopCountText = item.stopCount.toString(),
        marginText = "%.2f".format(item.margin),
        graceDays = item.graceDays.toString(),
        cost = "%.2f".format(item.cost),
        summary = item.summary,
        phone = truncate(item.phone, 120),
        dock = item.dock,
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerProfileViews(items: MutableList<TrailerProfile>): MutableList<TrailerProfileView> {
    val views = mutableListOf<TrailerProfileView>()
    for (item in items) {
        views.add(toTrailerProfileView(item))
    }
    return views
}

fun sumTrailerProfileView(view: TrailerProfileView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedStopCount.toDouble() + parsedMargin + parsedGraceDays.toDouble()
}
