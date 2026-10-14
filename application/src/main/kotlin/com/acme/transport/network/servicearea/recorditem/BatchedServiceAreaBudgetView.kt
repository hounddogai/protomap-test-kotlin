package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched service area budget. */
data class BatchedServiceAreaBudgetView(
    var code: String,
    var title: String,
    var contactEmailText: String,
    var revision: String,
    var ownerIdText: String,
    var threshold: String,
    var windowDaysText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toBatchedServiceAreaBudgetView(item: BatchedServiceAreaBudget): BatchedServiceAreaBudgetView {
    return BatchedServiceAreaBudgetView(
        code = item.code,
        title = item.title,
        contactEmailText = truncate(item.contactEmail, 80),
        revision = item.revision.toString(),
        ownerIdText = truncate(item.ownerId, 80),
        threshold = "%.2f".format(item.threshold),
        windowDaysText = item.windowDays.toString(),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedServiceAreaBudgetViews(items: MutableList<BatchedServiceAreaBudget>): MutableList<BatchedServiceAreaBudgetView> {
    val views = mutableListOf<BatchedServiceAreaBudgetView>()
    for (item in items) {
        views.add(toBatchedServiceAreaBudgetView(item))
    }
    return views
}

fun sumBatchedServiceAreaBudgetView(view: BatchedServiceAreaBudgetView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedRevision.toDouble() + parsedThreshold + parsedWindowDays.toDouble()
}
