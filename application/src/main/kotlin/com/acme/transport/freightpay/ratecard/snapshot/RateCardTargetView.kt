package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card target. */
data class RateCardTargetView(
    var code: String,
    var name: String,
    var retries: String,
    var threshold: String,
    var rate: String,
    var phone: String,
    var ceiling: String,
    var category: String,
    var source: String,
    var statusLabel: String,
)

fun toRateCardTargetView(item: RateCardTarget): RateCardTargetView {
    return RateCardTargetView(
        code = item.code,
        name = truncate(item.name, 120),
        retries = item.retries.toString(),
        threshold = "%.2f".format(item.threshold),
        rate = "%.2f".format(item.rate),
        phone = item.phone,
        ceiling = "%.2f".format(item.ceiling),
        category = truncate(item.category, 120),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardTargetViews(items: MutableList<RateCardTarget>): MutableList<RateCardTargetView> {
    val views = mutableListOf<RateCardTargetView>()
    for (item in items) {
        views.add(toRateCardTargetView(item))
    }
    return views
}

fun sumRateCardTargetView(view: RateCardTargetView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedRetries.toDouble() + parsedThreshold + parsedRate
}
