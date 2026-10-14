package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock summary. */
data class CrossDockSummaryView(
    var code: String,
    var label: String,
    var stopCount: String,
    var currency: String,
    var archivedText: String,
    var segmentText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toCrossDockSummaryView(item: CrossDockSummary): CrossDockSummaryView {
    return CrossDockSummaryView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 64),
        stopCount = item.stopCount.toString(),
        currency = truncate(item.currency, 64),
        archivedText = "${item.archived}",
        segmentText = item.segment,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockSummaryViews(items: MutableList<CrossDockSummary>): MutableList<CrossDockSummaryView> {
    val views = mutableListOf<CrossDockSummaryView>()
    for (item in items) {
        views.add(toCrossDockSummaryView(item))
    }
    return views
}

fun sumCrossDockSummaryView(view: CrossDockSummaryView): Double {
    val parsedStopCount = view.stopCount.toInt()
    return parsedStopCount.toDouble()
}
