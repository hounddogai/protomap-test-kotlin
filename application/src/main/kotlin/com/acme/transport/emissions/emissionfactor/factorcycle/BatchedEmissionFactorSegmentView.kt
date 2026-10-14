package com.acme.transport.emissions.emissionfactor.factorcycle

/** How screens and exports show a batched emission factor segment. */
data class BatchedEmissionFactorSegmentView(
    var code: String,
    var title: String,
    var windowDays: String,
    var amountText: String,
    var archived: String,
    var active: String,
    var threshold: String,
    var unit: String,
    var statusLabel: String,
)

fun toBatchedEmissionFactorSegmentView(item: BatchedEmissionFactorSegment): BatchedEmissionFactorSegmentView {
    return BatchedEmissionFactorSegmentView(
        code = item.code,
        title = item.title,
        windowDays = item.windowDays.toString(),
        amountText = "%.2f".format(item.amount),
        archived = "${item.archived}",
        active = "${item.active}",
        threshold = "%.2f".format(item.threshold),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedEmissionFactorSegmentViews(items: MutableList<BatchedEmissionFactorSegment>): MutableList<BatchedEmissionFactorSegmentView> {
    val views = mutableListOf<BatchedEmissionFactorSegmentView>()
    for (item in items) {
        views.add(toBatchedEmissionFactorSegmentView(item))
    }
    return views
}

fun sumBatchedEmissionFactorSegmentView(view: BatchedEmissionFactorSegmentView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedWindowDays.toDouble() + parsedAmount + parsedThreshold
}
