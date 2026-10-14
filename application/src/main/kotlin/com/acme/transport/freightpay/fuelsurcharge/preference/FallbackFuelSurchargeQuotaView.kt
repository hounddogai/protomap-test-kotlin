package com.acme.transport.freightpay.fuelsurcharge.preference

/** How screens and exports show a fallback fuel surcharge quota. */
data class FallbackFuelSurchargeQuotaView(
    var code: String,
    var name: String,
    var channelText: String,
    var trailerId: String,
    var loadKgText: String,
    var endsAt: String,
    var pinnedText: String,
    var currency: String,
    var statusLabel: String,
)

fun toFallbackFuelSurchargeQuotaView(item: FallbackFuelSurchargeQuota): FallbackFuelSurchargeQuotaView {
    return FallbackFuelSurchargeQuotaView(
        code = item.code,
        name = item.name,
        channelText = item.channel,
        trailerId = item.trailerId,
        loadKgText = "%.2f".format(item.loadKg),
        endsAt = item.endsAt.toString(),
        pinnedText = "${item.pinned}",
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackFuelSurchargeQuotaViews(items: MutableList<FallbackFuelSurchargeQuota>): MutableList<FallbackFuelSurchargeQuotaView> {
    val views = mutableListOf<FallbackFuelSurchargeQuotaView>()
    for (item in items) {
        views.add(toFallbackFuelSurchargeQuotaView(item))
    }
    return views
}

fun sumFallbackFuelSurchargeQuotaView(view: FallbackFuelSurchargeQuotaView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedLoadKg + parsedEndsAt.toDouble()
}
