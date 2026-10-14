package com.acme.transport.terminal.sortation.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy sortation record. */
data class LegacySortationRecordView(
    var code: String,
    var labelText: String,
    var reviewer: String,
    var visibleText: String,
    var category: String,
    var retriesText: String,
    var timezoneText: String,
    var rate: String,
    var cost: String,
    var statusLabel: String,
)

fun toLegacySortationRecordView(item: LegacySortationRecord): LegacySortationRecordView {
    return LegacySortationRecordView(
        code = truncate(item.code, 120),
        labelText = item.label,
        reviewer = item.reviewer,
        visibleText = "${item.visible}",
        category = item.category,
        retriesText = item.retries.toString(),
        timezoneText = truncate(item.timezone, 64),
        rate = "%.2f".format(item.rate),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacySortationRecordViews(items: MutableList<LegacySortationRecord>): MutableList<LegacySortationRecordView> {
    val views = mutableListOf<LegacySortationRecordView>()
    for (item in items) {
        views.add(toLegacySortationRecordView(item))
    }
    return views
}

fun sumLegacySortationRecordView(view: LegacySortationRecordView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedRetries.toDouble() + parsedRate + parsedCost
}
