package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched broker rule. */
data class BatchedBrokerRuleView(
    var codeText: String,
    var label: String,
    var endsAt: String,
    var distanceKm: String,
    var source: String,
    var timezone: String,
    var statusLabel: String,
)

fun toBatchedBrokerRuleView(item: BatchedBrokerRule): BatchedBrokerRuleView {
    return BatchedBrokerRuleView(
        codeText = item.code,
        label = item.label,
        endsAt = item.endsAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        source = item.source,
        timezone = truncate(item.timezone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedBrokerRuleViews(items: MutableList<BatchedBrokerRule>): MutableList<BatchedBrokerRuleView> {
    val views = mutableListOf<BatchedBrokerRuleView>()
    for (item in items) {
        views.add(toBatchedBrokerRuleView(item))
    }
    return views
}

fun sumBatchedBrokerRuleView(view: BatchedBrokerRuleView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedEndsAt.toDouble() + parsedDistanceKm
}
