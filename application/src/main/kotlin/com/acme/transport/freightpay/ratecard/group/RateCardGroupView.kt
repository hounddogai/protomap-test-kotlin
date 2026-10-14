package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card group. */
data class RateCardGroupView(
    var code: String,
    var titleText: String,
    var ratioText: String,
    var windowDays: String,
    var minimumText: String,
    var carrierText: String,
    var threshold: String,
    var depot: String,
    var vehicleId: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toRateCardGroupView(item: RateCardGroup): RateCardGroupView {
    return RateCardGroupView(
        code = truncate(item.code, 40),
        titleText = item.title,
        ratioText = "%.2f".format(item.ratio),
        windowDays = item.windowDays.toString(),
        minimumText = "%.2f".format(item.minimum),
        carrierText = truncate(item.carrier, 64),
        threshold = "%.2f".format(item.threshold),
        depot = item.depot,
        vehicleId = item.vehicleId,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardGroupViews(items: MutableList<RateCardGroup>): MutableList<RateCardGroupView> {
    val views = mutableListOf<RateCardGroupView>()
    for (item in items) {
        views.add(toRateCardGroupView(item))
    }
    return views
}

fun sumRateCardGroupView(view: RateCardGroupView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedRatio + parsedWindowDays.toDouble() + parsedMinimum
}
