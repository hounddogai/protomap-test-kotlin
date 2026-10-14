package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service area quota. */
data class FallbackServiceAreaQuotaView(
    var code: String,
    var title: String,
    var stopCountText: String,
    var currency: String,
    var limitText: String,
    var visible: String,
    var unit: String,
    var pallets: String,
    var statusLabel: String,
)

fun toFallbackServiceAreaQuotaView(item: FallbackServiceAreaQuota): FallbackServiceAreaQuotaView {
    return FallbackServiceAreaQuotaView(
        code = item.code,
        title = truncate(item.title, 40),
        stopCountText = item.stopCount.toString(),
        currency = truncate(item.currency, 40),
        limitText = item.limit.toString(),
        visible = "${item.visible}",
        unit = item.unit,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServiceAreaQuotaViews(items: MutableList<FallbackServiceAreaQuota>): MutableList<FallbackServiceAreaQuotaView> {
    val views = mutableListOf<FallbackServiceAreaQuotaView>()
    for (item in items) {
        views.add(toFallbackServiceAreaQuotaView(item))
    }
    return views
}

fun sumFallbackServiceAreaQuotaView(view: FallbackServiceAreaQuotaView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedStopCount.toDouble() + parsedLimit.toDouble() + parsedPallets.toDouble()
}
