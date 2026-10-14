package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer cycle. */
data class TrailerCycleView(
    var codeText: String,
    var titleText: String,
    var palletsText: String,
    var cost: String,
    var retries: String,
    var trailerId: String,
    var carrier: String,
    var timezone: String,
    var countryText: String,
    var statusLabel: String,
)

fun toTrailerCycleView(item: TrailerCycle): TrailerCycleView {
    return TrailerCycleView(
        codeText = item.code,
        titleText = item.title,
        palletsText = item.pallets.toString(),
        cost = "%.2f".format(item.cost),
        retries = item.retries.toString(),
        trailerId = truncate(item.trailerId, 120),
        carrier = item.carrier,
        timezone = item.timezone,
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerCycleViews(items: MutableList<TrailerCycle>): MutableList<TrailerCycleView> {
    val views = mutableListOf<TrailerCycleView>()
    for (item in items) {
        views.add(toTrailerCycleView(item))
    }
    return views
}

fun sumTrailerCycleView(view: TrailerCycleView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedPallets.toDouble() + parsedCost + parsedRetries.toDouble()
}
