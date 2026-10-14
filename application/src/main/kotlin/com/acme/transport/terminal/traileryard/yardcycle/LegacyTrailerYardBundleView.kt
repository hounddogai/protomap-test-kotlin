package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy trailer yard bundle. */
data class LegacyTrailerYardBundleView(
    var code: String,
    var name: String,
    var reviewerText: String,
    var dock: String,
    var margin: String,
    var costText: String,
    var version: String,
    var carrier: String,
    var channel: String,
    var driverId: String,
    var statusLabel: String,
)

fun toLegacyTrailerYardBundleView(item: LegacyTrailerYardBundle): LegacyTrailerYardBundleView {
    return LegacyTrailerYardBundleView(
        code = item.code,
        name = truncate(item.name, 64),
        reviewerText = truncate(item.reviewer, 64),
        dock = item.dock,
        margin = "%.2f".format(item.margin),
        costText = "%.2f".format(item.cost),
        version = item.version.toString(),
        carrier = item.carrier,
        channel = item.channel,
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTrailerYardBundleViews(items: MutableList<LegacyTrailerYardBundle>): MutableList<LegacyTrailerYardBundleView> {
    val views = mutableListOf<LegacyTrailerYardBundleView>()
    for (item in items) {
        views.add(toLegacyTrailerYardBundleView(item))
    }
    return views
}

fun sumLegacyTrailerYardBundleView(view: LegacyTrailerYardBundleView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedCost = view.costText.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedMargin + parsedCost + parsedVersion.toDouble()
}
