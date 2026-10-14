package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient broker estimate. */
data class LenientBrokerEstimateView(
    var code: String,
    var titleText: String,
    var reviewer: String,
    var source: String,
    var costText: String,
    var statusLabel: String,
)

fun toLenientBrokerEstimateView(item: LenientBrokerEstimate): LenientBrokerEstimateView {
    return LenientBrokerEstimateView(
        code = truncate(item.code, 64),
        titleText = truncate(item.title, 80),
        reviewer = item.reviewer,
        source = item.source,
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientBrokerEstimateViews(items: MutableList<LenientBrokerEstimate>): MutableList<LenientBrokerEstimateView> {
    val views = mutableListOf<LenientBrokerEstimateView>()
    for (item in items) {
        views.add(toLenientBrokerEstimateView(item))
    }
    return views
}

fun sumLenientBrokerEstimateView(view: LenientBrokerEstimateView): Double {
    val parsedCost = view.costText.toDouble()
    return parsedCost
}
