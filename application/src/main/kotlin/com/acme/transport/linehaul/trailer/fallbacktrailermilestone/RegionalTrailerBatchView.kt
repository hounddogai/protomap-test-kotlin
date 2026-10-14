package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional trailer batch. */
data class RegionalTrailerBatchView(
    var code: String,
    var titleText: String,
    var region: String,
    var unitText: String,
    var trailerId: String,
    var channel: String,
    var checksumText: String,
    var contactEmail: String,
    var countryText: String,
    var active: String,
    var quantity: String,
    var statusLabel: String,
)

fun toRegionalTrailerBatchView(item: RegionalTrailerBatch): RegionalTrailerBatchView {
    return RegionalTrailerBatchView(
        code = item.code,
        titleText = item.title,
        region = item.region,
        unitText = item.unit,
        trailerId = truncate(item.trailerId, 40),
        channel = item.channel,
        checksumText = item.checksum,
        contactEmail = truncate(item.contactEmail, 80),
        countryText = truncate(item.country, 120),
        active = "${item.active}",
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalTrailerBatchViews(items: MutableList<RegionalTrailerBatch>): MutableList<RegionalTrailerBatchView> {
    val views = mutableListOf<RegionalTrailerBatchView>()
    for (item in items) {
        views.add(toRegionalTrailerBatchView(item))
    }
    return views
}

fun sumRegionalTrailerBatchView(view: RegionalTrailerBatchView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
