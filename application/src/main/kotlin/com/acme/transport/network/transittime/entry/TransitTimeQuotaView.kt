package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time quota. */
data class TransitTimeQuotaView(
    var codeText: String,
    var title: String,
    var updatedAtText: String,
    var rateText: String,
    var maximum: String,
    var amountText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toTransitTimeQuotaView(item: TransitTimeQuota): TransitTimeQuotaView {
    return TransitTimeQuotaView(
        codeText = truncate(item.code, 64),
        title = item.title,
        updatedAtText = item.updatedAt.toString(),
        rateText = "%.2f".format(item.rate),
        maximum = "%.2f".format(item.maximum),
        amountText = "%.2f".format(item.amount),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeQuotaViews(items: MutableList<TransitTimeQuota>): MutableList<TransitTimeQuotaView> {
    val views = mutableListOf<TransitTimeQuotaView>()
    for (item in items) {
        views.add(toTransitTimeQuotaView(item))
    }
    return views
}

fun sumTransitTimeQuotaView(view: TransitTimeQuotaView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedRate = view.rateText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedUpdatedAt.toDouble() + parsedRate + parsedMaximum
}
