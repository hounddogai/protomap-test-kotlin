package com.acme.transport.emissions.emissionfactor.request

/** How screens and exports show a batched emission factor batch. */
data class BatchedEmissionFactorBatchView(
    var codeText: String,
    var name: String,
    var floorValueText: String,
    var thresholdText: String,
    var margin: String,
    var limit: String,
    var summaryText: String,
    var depot: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toBatchedEmissionFactorBatchView(item: BatchedEmissionFactorBatch): BatchedEmissionFactorBatchView {
    return BatchedEmissionFactorBatchView(
        codeText = item.code,
        name = item.name,
        floorValueText = "%.2f".format(item.floorValue),
        thresholdText = "%.2f".format(item.threshold),
        margin = "%.2f".format(item.margin),
        limit = item.limit.toString(),
        summaryText = item.summary,
        depot = item.depot,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedEmissionFactorBatchViews(items: MutableList<BatchedEmissionFactorBatch>): MutableList<BatchedEmissionFactorBatchView> {
    val views = mutableListOf<BatchedEmissionFactorBatchView>()
    for (item in items) {
        views.add(toBatchedEmissionFactorBatchView(item))
    }
    return views
}

fun sumBatchedEmissionFactorBatchView(view: BatchedEmissionFactorBatchView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedFloorValue + parsedThreshold + parsedMargin
}
