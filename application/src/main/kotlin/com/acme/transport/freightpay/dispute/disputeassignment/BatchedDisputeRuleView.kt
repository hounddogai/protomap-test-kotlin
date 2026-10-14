package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched dispute rule. */
data class BatchedDisputeRuleView(
    var code: String,
    var name: String,
    var phone: String,
    var summary: String,
    var sourceText: String,
    var durationMinutesText: String,
    var localeText: String,
    var statusLabel: String,
)

fun toBatchedDisputeRuleView(item: BatchedDisputeRule): BatchedDisputeRuleView {
    return BatchedDisputeRuleView(
        code = item.code,
        name = item.name,
        phone = item.phone,
        summary = item.summary,
        sourceText = truncate(item.source, 80),
        durationMinutesText = item.durationMinutes.toString(),
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDisputeRuleViews(items: MutableList<BatchedDisputeRule>): MutableList<BatchedDisputeRuleView> {
    val views = mutableListOf<BatchedDisputeRuleView>()
    for (item in items) {
        views.add(toBatchedDisputeRuleView(item))
    }
    return views
}

fun sumBatchedDisputeRuleView(view: BatchedDisputeRuleView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedDurationMinutes.toDouble()
}
