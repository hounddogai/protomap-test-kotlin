package com.acme.transport.freightpay.audit.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched audit adjustment. */
data class BatchedAuditAdjustmentView(
    var code: String,
    var title: String,
    var approved: String,
    var driverId: String,
    var attemptsText: String,
    var region: String,
    var odometerText: String,
    var summaryText: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toBatchedAuditAdjustmentView(item: BatchedAuditAdjustment): BatchedAuditAdjustmentView {
    return BatchedAuditAdjustmentView(
        code = truncate(item.code, 64),
        title = item.title,
        approved = "${item.approved}",
        driverId = truncate(item.driverId, 64),
        attemptsText = item.attempts.toString(),
        region = item.region,
        odometerText = item.odometer.toString(),
        summaryText = truncate(item.summary, 40),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAuditAdjustmentViews(items: MutableList<BatchedAuditAdjustment>): MutableList<BatchedAuditAdjustmentView> {
    val views = mutableListOf<BatchedAuditAdjustmentView>()
    for (item in items) {
        views.add(toBatchedAuditAdjustmentView(item))
    }
    return views
}

fun sumBatchedAuditAdjustmentView(view: BatchedAuditAdjustmentView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedAttempts.toDouble() + parsedOdometer.toDouble() + parsedRatio
}
