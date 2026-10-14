package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient hub estimate. */
data class LenientHubEstimateView(
    var codeText: String,
    var name: String,
    var costText: String,
    var externalRefText: String,
    var pallets: String,
    var stopCount: String,
    var carrier: String,
    var statusLabel: String,
)

fun toLenientHubEstimateView(item: LenientHubEstimate): LenientHubEstimateView {
    return LenientHubEstimateView(
        codeText = item.code,
        name = truncate(item.name, 40),
        costText = "%.2f".format(item.cost),
        externalRefText = item.externalRef,
        pallets = item.pallets.toString(),
        stopCount = item.stopCount.toString(),
        carrier = truncate(item.carrier, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientHubEstimateViews(items: MutableList<LenientHubEstimate>): MutableList<LenientHubEstimateView> {
    val views = mutableListOf<LenientHubEstimateView>()
    for (item in items) {
        views.add(toLenientHubEstimateView(item))
    }
    return views
}

fun sumLenientHubEstimateView(view: LenientHubEstimateView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedCost + parsedPallets.toDouble() + parsedStopCount.toDouble()
}
