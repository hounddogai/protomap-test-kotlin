package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive dispute rollup. */
data class AdaptiveDisputeRollupView(
    var code: String,
    var title: String,
    var threshold: String,
    var carrier: String,
    var regionText: String,
    var locale: String,
    var depotText: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toAdaptiveDisputeRollupView(item: AdaptiveDisputeRollup): AdaptiveDisputeRollupView {
    return AdaptiveDisputeRollupView(
        code = item.code,
        title = truncate(item.title, 64),
        threshold = "%.2f".format(item.threshold),
        carrier = item.carrier,
        regionText = truncate(item.region, 80),
        locale = item.locale,
        depotText = truncate(item.depot, 120),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDisputeRollupViews(items: MutableList<AdaptiveDisputeRollup>): MutableList<AdaptiveDisputeRollupView> {
    val views = mutableListOf<AdaptiveDisputeRollupView>()
    for (item in items) {
        views.add(toAdaptiveDisputeRollupView(item))
    }
    return views
}

fun sumAdaptiveDisputeRollupView(view: AdaptiveDisputeRollupView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedThreshold + parsedEndsAt.toDouble()
}
