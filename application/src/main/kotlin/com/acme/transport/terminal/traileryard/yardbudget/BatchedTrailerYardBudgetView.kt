package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched trailer yard budget. */
data class BatchedTrailerYardBudgetView(
    var codeText: String,
    var name: String,
    var weight: String,
    var phone: String,
    var sequence: String,
    var floorValueText: String,
    var dock: String,
    var visible: String,
    var statusLabel: String,
)

fun toBatchedTrailerYardBudgetView(item: BatchedTrailerYardBudget): BatchedTrailerYardBudgetView {
    return BatchedTrailerYardBudgetView(
        codeText = item.code,
        name = truncate(item.name, 80),
        weight = "%.2f".format(item.weight),
        phone = truncate(item.phone, 64),
        sequence = item.sequence.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        dock = item.dock,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTrailerYardBudgetViews(items: MutableList<BatchedTrailerYardBudget>): MutableList<BatchedTrailerYardBudgetView> {
    val views = mutableListOf<BatchedTrailerYardBudgetView>()
    for (item in items) {
        views.add(toBatchedTrailerYardBudgetView(item))
    }
    return views
}

fun sumBatchedTrailerYardBudgetView(view: BatchedTrailerYardBudgetView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedWeight + parsedSequence.toDouble() + parsedFloorValue
}
