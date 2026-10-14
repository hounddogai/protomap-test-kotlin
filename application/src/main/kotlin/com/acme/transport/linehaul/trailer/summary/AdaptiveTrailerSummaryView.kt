package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive trailer summary. */
data class AdaptiveTrailerSummaryView(
    var code: String,
    var name: String,
    var windowDays: String,
    var channelText: String,
    var quantity: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toAdaptiveTrailerSummaryView(item: AdaptiveTrailerSummary): AdaptiveTrailerSummaryView {
    return AdaptiveTrailerSummaryView(
        code = item.code,
        name = truncate(item.name, 80),
        windowDays = item.windowDays.toString(),
        channelText = truncate(item.channel, 80),
        quantity = item.quantity.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTrailerSummaryViews(items: MutableList<AdaptiveTrailerSummary>): MutableList<AdaptiveTrailerSummaryView> {
    val views = mutableListOf<AdaptiveTrailerSummaryView>()
    for (item in items) {
        views.add(toAdaptiveTrailerSummaryView(item))
    }
    return views
}

fun sumAdaptiveTrailerSummaryView(view: AdaptiveTrailerSummaryView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedWindowDays.toDouble() + parsedQuantity.toDouble() + parsedCeiling
}
