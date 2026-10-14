package com.acme.transport.linehaul.laneplan.plansegment

/** How screens and exports show a batched lane plan threshold. */
data class BatchedLanePlanThresholdView(
    var codeText: String,
    var name: String,
    var attempts: String,
    var palletsText: String,
    var vehicleIdText: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toBatchedLanePlanThresholdView(item: BatchedLanePlanThreshold): BatchedLanePlanThresholdView {
    return BatchedLanePlanThresholdView(
        codeText = item.code,
        name = item.name,
        attempts = item.attempts.toString(),
        palletsText = item.pallets.toString(),
        vehicleIdText = item.vehicleId,
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedLanePlanThresholdViews(items: MutableList<BatchedLanePlanThreshold>): MutableList<BatchedLanePlanThresholdView> {
    val views = mutableListOf<BatchedLanePlanThresholdView>()
    for (item in items) {
        views.add(toBatchedLanePlanThresholdView(item))
    }
    return views
}

fun sumBatchedLanePlanThresholdView(view: BatchedLanePlanThresholdView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedAttempts.toDouble() + parsedPallets.toDouble()
}
