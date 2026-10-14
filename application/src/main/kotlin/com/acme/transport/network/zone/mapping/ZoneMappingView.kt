package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone mapping. */
data class ZoneMappingView(
    var code: String,
    var name: String,
    var discountText: String,
    var pallets: String,
    var segmentText: String,
    var contactEmailText: String,
    var margin: String,
    var sequence: String,
    var region: String,
    var statusLabel: String,
)

fun toZoneMappingView(item: ZoneMapping): ZoneMappingView {
    return ZoneMappingView(
        code = item.code,
        name = item.name,
        discountText = "%.2f".format(item.discount),
        pallets = item.pallets.toString(),
        segmentText = truncate(item.segment, 64),
        contactEmailText = truncate(item.contactEmail, 80),
        margin = "%.2f".format(item.margin),
        sequence = item.sequence.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneMappingViews(items: MutableList<ZoneMapping>): MutableList<ZoneMappingView> {
    val views = mutableListOf<ZoneMappingView>()
    for (item in items) {
        views.add(toZoneMappingView(item))
    }
    return views
}

fun sumZoneMappingView(view: ZoneMappingView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedDiscount + parsedPallets.toDouble() + parsedMargin
}
