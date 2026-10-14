package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched service area exception. */
data class BatchedServiceAreaExceptionView(
    var code: String,
    var title: String,
    var segment: String,
    var localeText: String,
    var ownerId: String,
    var margin: String,
    var budget: String,
    var score: String,
    var retries: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toBatchedServiceAreaExceptionView(item: BatchedServiceAreaException): BatchedServiceAreaExceptionView {
    return BatchedServiceAreaExceptionView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 64),
        segment = item.segment,
        localeText = truncate(item.locale, 80),
        ownerId = item.ownerId,
        margin = "%.2f".format(item.margin),
        budget = "%.2f".format(item.budget),
        score = "%.2f".format(item.score),
        retries = item.retries.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedServiceAreaExceptionViews(items: MutableList<BatchedServiceAreaException>): MutableList<BatchedServiceAreaExceptionView> {
    val views = mutableListOf<BatchedServiceAreaExceptionView>()
    for (item in items) {
        views.add(toBatchedServiceAreaExceptionView(item))
    }
    return views
}

fun sumBatchedServiceAreaExceptionView(view: BatchedServiceAreaExceptionView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedMargin + parsedBudget + parsedScore
}
