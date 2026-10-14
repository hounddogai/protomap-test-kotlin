package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback ev charge mapping. */
data class FallbackEvChargeMappingView(
    var code: String,
    var title: String,
    var endsAtText: String,
    var cost: String,
    var rate: String,
    var region: String,
    var currency: String,
    var segment: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toFallbackEvChargeMappingView(item: FallbackEvChargeMapping): FallbackEvChargeMappingView {
    return FallbackEvChargeMappingView(
        code = item.code,
        title = item.title,
        endsAtText = item.endsAt.toString(),
        cost = "%.2f".format(item.cost),
        rate = "%.2f".format(item.rate),
        region = truncate(item.region, 64),
        currency = item.currency,
        segment = item.segment,
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEvChargeMappingViews(items: MutableList<FallbackEvChargeMapping>): MutableList<FallbackEvChargeMappingView> {
    val views = mutableListOf<FallbackEvChargeMappingView>()
    for (item in items) {
        views.add(toFallbackEvChargeMappingView(item))
    }
    return views
}

fun sumFallbackEvChargeMappingView(view: FallbackEvChargeMappingView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedEndsAt.toDouble() + parsedCost + parsedRate
}
