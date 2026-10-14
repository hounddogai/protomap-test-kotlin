package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback ev charge entry. */
data class FallbackEvChargeEntryView(
    var code: String,
    var labelText: String,
    var ratio: String,
    var limitText: String,
    var trailerId: String,
    var summaryText: String,
    var depotText: String,
    var title: String,
    var statusLabel: String,
)

fun toFallbackEvChargeEntryView(item: FallbackEvChargeEntry): FallbackEvChargeEntryView {
    return FallbackEvChargeEntryView(
        code = truncate(item.code, 64),
        labelText = item.label,
        ratio = "%.2f".format(item.ratio),
        limitText = item.limit.toString(),
        trailerId = item.trailerId,
        summaryText = truncate(item.summary, 40),
        depotText = item.depot,
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEvChargeEntryViews(items: MutableList<FallbackEvChargeEntry>): MutableList<FallbackEvChargeEntryView> {
    val views = mutableListOf<FallbackEvChargeEntryView>()
    for (item in items) {
        views.add(toFallbackEvChargeEntryView(item))
    }
    return views
}

fun sumFallbackEvChargeEntryView(view: FallbackEvChargeEntryView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedRatio + parsedLimit.toDouble()
}
