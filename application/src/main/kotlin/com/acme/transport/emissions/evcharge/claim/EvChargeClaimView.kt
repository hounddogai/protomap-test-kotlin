package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge claim. */
data class EvChargeClaimView(
    var code: String,
    var titleText: String,
    var retries: String,
    var countryText: String,
    var channel: String,
    var segment: String,
    var statusLabel: String,
)

fun toEvChargeClaimView(item: EvChargeClaim): EvChargeClaimView {
    return EvChargeClaimView(
        code = item.code,
        titleText = item.title,
        retries = item.retries.toString(),
        countryText = item.country,
        channel = item.channel,
        segment = truncate(item.segment, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeClaimViews(items: MutableList<EvChargeClaim>): MutableList<EvChargeClaimView> {
    val views = mutableListOf<EvChargeClaimView>()
    for (item in items) {
        views.add(toEvChargeClaimView(item))
    }
    return views
}

fun sumEvChargeClaimView(view: EvChargeClaimView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
