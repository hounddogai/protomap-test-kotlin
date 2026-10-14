package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending cross dock summary. */
data class PendingCrossDockSummaryView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var startsAt: String,
    var sequence: String,
    var endsAt: String,
    var routeCode: String,
    var category: String,
    var statusLabel: String,
)

fun toPendingCrossDockSummaryView(item: PendingCrossDockSummary): PendingCrossDockSummaryView {
    return PendingCrossDockSummaryView(
        code = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        startsAt = item.startsAt.toString(),
        sequence = item.sequence.toString(),
        endsAt = item.endsAt.toString(),
        routeCode = item.routeCode,
        category = truncate(item.category, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCrossDockSummaryViews(items: MutableList<PendingCrossDockSummary>): MutableList<PendingCrossDockSummaryView> {
    val views = mutableListOf<PendingCrossDockSummaryView>()
    for (item in items) {
        views.add(toPendingCrossDockSummaryView(item))
    }
    return views
}

fun sumPendingCrossDockSummaryView(view: PendingCrossDockSummaryView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedEtaMinutes.toDouble() + parsedStartsAt.toDouble() + parsedSequence.toDouble()
}
