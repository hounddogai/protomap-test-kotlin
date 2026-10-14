package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard bundle. */
data class TrailerYardBundleView(
    var code: String,
    var nameText: String,
    var stopCountText: String,
    var trailerIdText: String,
    var shiftCode: String,
    var timezone: String,
    var margin: String,
    var discountText: String,
    var label: String,
    var retries: String,
    var source: String,
    var statusLabel: String,
)

fun toTrailerYardBundleView(item: TrailerYardBundle): TrailerYardBundleView {
    return TrailerYardBundleView(
        code = item.code,
        nameText = truncate(item.name, 80),
        stopCountText = item.stopCount.toString(),
        trailerIdText = item.trailerId,
        shiftCode = truncate(item.shiftCode, 80),
        timezone = item.timezone,
        margin = "%.2f".format(item.margin),
        discountText = "%.2f".format(item.discount),
        label = truncate(item.label, 64),
        retries = item.retries.toString(),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardBundleViews(items: MutableList<TrailerYardBundle>): MutableList<TrailerYardBundleView> {
    val views = mutableListOf<TrailerYardBundleView>()
    for (item in items) {
        views.add(toTrailerYardBundleView(item))
    }
    return views
}

fun sumTrailerYardBundleView(view: TrailerYardBundleView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedStopCount.toDouble() + parsedMargin + parsedDiscount
}
