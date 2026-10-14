package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy rate card segment. */
data class LegacyRateCardSegmentView(
    var codeText: String,
    var label: String,
    var ownerId: String,
    var segment: String,
    var maximumText: String,
    var rate: String,
    var driverId: String,
    var phoneText: String,
    var lane: String,
    var statusLabel: String,
)

fun toLegacyRateCardSegmentView(item: LegacyRateCardSegment): LegacyRateCardSegmentView {
    return LegacyRateCardSegmentView(
        codeText = truncate(item.code, 40),
        label = truncate(item.label, 64),
        ownerId = item.ownerId,
        segment = item.segment,
        maximumText = "%.2f".format(item.maximum),
        rate = "%.2f".format(item.rate),
        driverId = item.driverId,
        phoneText = item.phone,
        lane = truncate(item.lane, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyRateCardSegmentViews(items: MutableList<LegacyRateCardSegment>): MutableList<LegacyRateCardSegmentView> {
    val views = mutableListOf<LegacyRateCardSegmentView>()
    for (item in items) {
        views.add(toLegacyRateCardSegmentView(item))
    }
    return views
}

fun sumLegacyRateCardSegmentView(view: LegacyRateCardSegmentView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedMaximum + parsedRate
}
