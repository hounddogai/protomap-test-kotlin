package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card digest. */
data class RateCardDigestView(
    var codeText: String,
    var label: String,
    var ceiling: String,
    var ratioText: String,
    var activeText: String,
    var floorValue: String,
    var timezoneText: String,
    var graceDays: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toRateCardDigestView(item: RateCardDigest): RateCardDigestView {
    return RateCardDigestView(
        codeText = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        ratioText = "%.2f".format(item.ratio),
        activeText = "${item.active}",
        floorValue = "%.2f".format(item.floorValue),
        timezoneText = truncate(item.timezone, 40),
        graceDays = item.graceDays.toString(),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardDigestViews(items: MutableList<RateCardDigest>): MutableList<RateCardDigestView> {
    val views = mutableListOf<RateCardDigestView>()
    for (item in items) {
        views.add(toRateCardDigestView(item))
    }
    return views
}

fun sumRateCardDigestView(view: RateCardDigestView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedCeiling + parsedRatio + parsedFloorValue
}
