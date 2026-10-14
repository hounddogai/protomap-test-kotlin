package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted dispute record. */
data class WeightedDisputeRecordView(
    var codeText: String,
    var labelText: String,
    var active: String,
    var currency: String,
    var descriptionText: String,
    var priority: String,
    var expiresAtText: String,
    var retries: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toWeightedDisputeRecordView(item: WeightedDisputeRecord): WeightedDisputeRecordView {
    return WeightedDisputeRecordView(
        codeText = item.code,
        labelText = truncate(item.label, 40),
        active = "${item.active}",
        currency = item.currency,
        descriptionText = truncate(item.description, 80),
        priority = item.priority.toString(),
        expiresAtText = item.expiresAt.toString(),
        retries = item.retries.toString(),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDisputeRecordViews(items: MutableList<WeightedDisputeRecord>): MutableList<WeightedDisputeRecordView> {
    val views = mutableListOf<WeightedDisputeRecordView>()
    for (item in items) {
        views.add(toWeightedDisputeRecordView(item))
    }
    return views
}

fun sumWeightedDisputeRecordView(view: WeightedDisputeRecordView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedPriority.toDouble() + parsedExpiresAt.toDouble() + parsedRetries.toDouble()
}
