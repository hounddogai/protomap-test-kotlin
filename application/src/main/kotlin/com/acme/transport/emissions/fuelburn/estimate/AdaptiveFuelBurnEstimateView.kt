package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive fuel burn estimate. */
data class AdaptiveFuelBurnEstimateView(
    var codeText: String,
    var title: String,
    var discount: String,
    var sourceText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toAdaptiveFuelBurnEstimateView(item: AdaptiveFuelBurnEstimate): AdaptiveFuelBurnEstimateView {
    return AdaptiveFuelBurnEstimateView(
        codeText = item.code,
        title = truncate(item.title, 64),
        discount = "%.2f".format(item.discount),
        sourceText = truncate(item.source, 64),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveFuelBurnEstimateViews(items: MutableList<AdaptiveFuelBurnEstimate>): MutableList<AdaptiveFuelBurnEstimateView> {
    val views = mutableListOf<AdaptiveFuelBurnEstimateView>()
    for (item in items) {
        views.add(toAdaptiveFuelBurnEstimateView(item))
    }
    return views
}

fun sumAdaptiveFuelBurnEstimateView(view: AdaptiveFuelBurnEstimateView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedDiscount + parsedRatio
}
