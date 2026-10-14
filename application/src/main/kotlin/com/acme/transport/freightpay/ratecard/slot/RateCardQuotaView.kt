package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card quota. */
data class RateCardQuotaView(
    var codeText: String,
    var label: String,
    var summary: String,
    var lane: String,
    var approvedText: String,
    var weight: String,
    var driverId: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toRateCardQuotaView(item: RateCardQuota): RateCardQuotaView {
    return RateCardQuotaView(
        codeText = truncate(item.code, 64),
        label = truncate(item.label, 40),
        summary = item.summary,
        lane = item.lane,
        approvedText = "${item.approved}",
        weight = "%.2f".format(item.weight),
        driverId = item.driverId,
        currencyText = truncate(item.currency, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardQuotaViews(items: MutableList<RateCardQuota>): MutableList<RateCardQuotaView> {
    val views = mutableListOf<RateCardQuotaView>()
    for (item in items) {
        views.add(toRateCardQuotaView(item))
    }
    return views
}

fun sumRateCardQuotaView(view: RateCardQuotaView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
