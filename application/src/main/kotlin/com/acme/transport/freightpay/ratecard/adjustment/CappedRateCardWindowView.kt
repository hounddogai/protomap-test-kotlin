package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped rate card window. */
data class CappedRateCardWindowView(
    var code: String,
    var title: String,
    var retries: String,
    var timezone: String,
    var labelText: String,
    var reviewer: String,
    var capacity: String,
    var version: String,
    var pallets: String,
    var weight: String,
    var statusLabel: String,
)

fun toCappedRateCardWindowView(item: CappedRateCardWindow): CappedRateCardWindowView {
    return CappedRateCardWindowView(
        code = item.code,
        title = truncate(item.title, 64),
        retries = item.retries.toString(),
        timezone = item.timezone,
        labelText = item.label,
        reviewer = item.reviewer,
        capacity = item.capacity.toString(),
        version = item.version.toString(),
        pallets = item.pallets.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRateCardWindowViews(items: MutableList<CappedRateCardWindow>): MutableList<CappedRateCardWindowView> {
    val views = mutableListOf<CappedRateCardWindowView>()
    for (item in items) {
        views.add(toCappedRateCardWindowView(item))
    }
    return views
}

fun sumCappedRateCardWindowView(view: CappedRateCardWindowView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedVersion = view.version.toInt()
    return parsedRetries.toDouble() + parsedCapacity.toDouble() + parsedVersion.toDouble()
}
