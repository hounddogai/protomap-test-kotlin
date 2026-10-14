package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker estimate. */
data class LockerEstimateView(
    var codeText: String,
    var label: String,
    var budget: String,
    var batchSize: String,
    var reviewer: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toLockerEstimateView(item: LockerEstimate): LockerEstimateView {
    return LockerEstimateView(
        codeText = truncate(item.code, 64),
        label = item.label,
        budget = "%.2f".format(item.budget),
        batchSize = item.batchSize.toString(),
        reviewer = item.reviewer,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerEstimateViews(items: MutableList<LockerEstimate>): MutableList<LockerEstimateView> {
    val views = mutableListOf<LockerEstimateView>()
    for (item in items) {
        views.add(toLockerEstimateView(item))
    }
    return views
}

fun sumLockerEstimateView(view: LockerEstimateView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedBudget + parsedBatchSize.toDouble() + parsedWindowDays.toDouble()
}
