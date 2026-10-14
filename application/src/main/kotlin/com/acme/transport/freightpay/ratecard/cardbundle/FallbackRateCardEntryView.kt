package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback rate card entry. */
data class FallbackRateCardEntryView(
    var code: String,
    var labelText: String,
    var locale: String,
    var segment: String,
    var archived: String,
    var retries: String,
    var pinnedText: String,
    var carrierText: String,
    var floorValue: String,
    var shiftCodeText: String,
    var country: String,
    var statusLabel: String,
)

fun toFallbackRateCardEntryView(item: FallbackRateCardEntry): FallbackRateCardEntryView {
    return FallbackRateCardEntryView(
        code = item.code,
        labelText = item.label,
        locale = truncate(item.locale, 40),
        segment = item.segment,
        archived = "${item.archived}",
        retries = item.retries.toString(),
        pinnedText = "${item.pinned}",
        carrierText = item.carrier,
        floorValue = "%.2f".format(item.floorValue),
        shiftCodeText = truncate(item.shiftCode, 64),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRateCardEntryViews(items: MutableList<FallbackRateCardEntry>): MutableList<FallbackRateCardEntryView> {
    val views = mutableListOf<FallbackRateCardEntryView>()
    for (item in items) {
        views.add(toFallbackRateCardEntryView(item))
    }
    return views
}

fun sumFallbackRateCardEntryView(view: FallbackRateCardEntryView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedRetries.toDouble() + parsedFloorValue
}
