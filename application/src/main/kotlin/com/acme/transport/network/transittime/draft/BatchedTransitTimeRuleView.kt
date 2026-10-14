package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched transit time rule. */
data class BatchedTransitTimeRuleView(
    var code: String,
    var label: String,
    var ceiling: String,
    var externalRefText: String,
    var startsAt: String,
    var graceDays: String,
    var updatedAt: String,
    var sourceText: String,
    var loadKg: String,
    var currency: String,
    var statusLabel: String,
)

fun toBatchedTransitTimeRuleView(item: BatchedTransitTimeRule): BatchedTransitTimeRuleView {
    return BatchedTransitTimeRuleView(
        code = item.code,
        label = truncate(item.label, 64),
        ceiling = "%.2f".format(item.ceiling),
        externalRefText = item.externalRef,
        startsAt = item.startsAt.toString(),
        graceDays = item.graceDays.toString(),
        updatedAt = item.updatedAt.toString(),
        sourceText = truncate(item.source, 40),
        loadKg = "%.2f".format(item.loadKg),
        currency = truncate(item.currency, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTransitTimeRuleViews(items: MutableList<BatchedTransitTimeRule>): MutableList<BatchedTransitTimeRuleView> {
    val views = mutableListOf<BatchedTransitTimeRuleView>()
    for (item in items) {
        views.add(toBatchedTransitTimeRuleView(item))
    }
    return views
}

fun sumBatchedTransitTimeRuleView(view: BatchedTransitTimeRuleView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCeiling + parsedStartsAt.toDouble() + parsedGraceDays.toDouble()
}
