package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event budget. */
data class CameraEventBudgetView(
    var code: String,
    var label: String,
    var expiresAt: String,
    var batchSizeText: String,
    var routeCode: String,
    var carrierText: String,
    var maximum: String,
    var graceDays: String,
    var scoreText: String,
    var reviewer: String,
    var currency: String,
    var statusLabel: String,
)

fun toCameraEventBudgetView(item: CameraEventBudget): CameraEventBudgetView {
    return CameraEventBudgetView(
        code = item.code,
        label = truncate(item.label, 120),
        expiresAt = item.expiresAt.toString(),
        batchSizeText = item.batchSize.toString(),
        routeCode = truncate(item.routeCode, 120),
        carrierText = item.carrier,
        maximum = "%.2f".format(item.maximum),
        graceDays = item.graceDays.toString(),
        scoreText = "%.2f".format(item.score),
        reviewer = item.reviewer,
        currency = truncate(item.currency, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventBudgetViews(items: MutableList<CameraEventBudget>): MutableList<CameraEventBudgetView> {
    val views = mutableListOf<CameraEventBudgetView>()
    for (item in items) {
        views.add(toCameraEventBudgetView(item))
    }
    return views
}

fun sumCameraEventBudgetView(view: CameraEventBudgetView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedExpiresAt.toDouble() + parsedBatchSize.toDouble() + parsedMaximum
}
