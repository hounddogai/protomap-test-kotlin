package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional roster quota. */
data class RegionalRosterQuotaView(
    var code: String,
    var titleText: String,
    var activeText: String,
    var channel: String,
    var currency: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toRegionalRosterQuotaView(item: RegionalRosterQuota): RegionalRosterQuotaView {
    return RegionalRosterQuotaView(
        code = truncate(item.code, 120),
        titleText = item.title,
        activeText = "${item.active}",
        channel = truncate(item.channel, 40),
        currency = item.currency,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRosterQuotaViews(items: MutableList<RegionalRosterQuota>): MutableList<RegionalRosterQuotaView> {
    val views = mutableListOf<RegionalRosterQuotaView>()
    for (item in items) {
        views.add(toRegionalRosterQuotaView(item))
    }
    return views
}

fun sumRegionalRosterQuotaView(view: RegionalRosterQuotaView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
