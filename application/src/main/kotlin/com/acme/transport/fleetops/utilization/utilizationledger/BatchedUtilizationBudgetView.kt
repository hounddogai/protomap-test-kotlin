package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched utilization budget. */
data class BatchedUtilizationBudgetView(
    var code: String,
    var name: String,
    var retries: String,
    var shiftCodeText: String,
    var distanceKmText: String,
    var channelText: String,
    var version: String,
    var statusLabel: String,
)

fun toBatchedUtilizationBudgetView(item: BatchedUtilizationBudget): BatchedUtilizationBudgetView {
    return BatchedUtilizationBudgetView(
        code = item.code,
        name = item.name,
        retries = item.retries.toString(),
        shiftCodeText = item.shiftCode,
        distanceKmText = "%.2f".format(item.distanceKm),
        channelText = truncate(item.channel, 80),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedUtilizationBudgetViews(items: MutableList<BatchedUtilizationBudget>): MutableList<BatchedUtilizationBudgetView> {
    val views = mutableListOf<BatchedUtilizationBudgetView>()
    for (item in items) {
        views.add(toBatchedUtilizationBudgetView(item))
    }
    return views
}

fun sumBatchedUtilizationBudgetView(view: BatchedUtilizationBudgetView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedRetries.toDouble() + parsedDistanceKm + parsedVersion.toDouble()
}
