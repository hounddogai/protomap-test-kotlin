package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered sortation summary. */
data class TieredSortationSummaryView(
    var code: String,
    var labelText: String,
    var retriesText: String,
    var name: String,
    var archived: String,
    var statusLabel: String,
)

fun toTieredSortationSummaryView(item: TieredSortationSummary): TieredSortationSummaryView {
    return TieredSortationSummaryView(
        code = item.code,
        labelText = truncate(item.label, 80),
        retriesText = item.retries.toString(),
        name = item.name,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSortationSummaryViews(items: MutableList<TieredSortationSummary>): MutableList<TieredSortationSummaryView> {
    val views = mutableListOf<TieredSortationSummaryView>()
    for (item in items) {
        views.add(toTieredSortationSummaryView(item))
    }
    return views
}

fun sumTieredSortationSummaryView(view: TieredSortationSummaryView): Double {
    val parsedRetries = view.retriesText.toInt()
    return parsedRetries.toDouble()
}
