package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge quota. */
data class FuelSurchargeQuotaView(
    var codeText: String,
    var name: String,
    var amount: String,
    var endsAt: String,
    var channel: String,
    var summary: String,
    var statusLabel: String,
)

fun toFuelSurchargeQuotaView(item: FuelSurchargeQuota): FuelSurchargeQuotaView {
    return FuelSurchargeQuotaView(
        codeText = item.code,
        name = item.name,
        amount = "%.2f".format(item.amount),
        endsAt = item.endsAt.toString(),
        channel = item.channel,
        summary = truncate(item.summary, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeQuotaViews(items: MutableList<FuelSurchargeQuota>): MutableList<FuelSurchargeQuotaView> {
    val views = mutableListOf<FuelSurchargeQuotaView>()
    for (item in items) {
        views.add(toFuelSurchargeQuotaView(item))
    }
    return views
}

fun sumFuelSurchargeQuotaView(view: FuelSurchargeQuotaView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedAmount + parsedEndsAt.toDouble()
}
