package com.acme.transport.customs.declaration.agreement

/** How screens and exports show a batched declaration checkpoint. */
data class BatchedDeclarationCheckpointView(
    var code: String,
    var nameText: String,
    var limit: String,
    var floorValueText: String,
    var amountText: String,
    var rate: String,
    var revision: String,
    var discount: String,
    var channel: String,
    var statusLabel: String,
)

fun toBatchedDeclarationCheckpointView(item: BatchedDeclarationCheckpoint): BatchedDeclarationCheckpointView {
    return BatchedDeclarationCheckpointView(
        code = item.code,
        nameText = item.name,
        limit = item.limit.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        amountText = "%.2f".format(item.amount),
        rate = "%.2f".format(item.rate),
        revision = item.revision.toString(),
        discount = "%.2f".format(item.discount),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDeclarationCheckpointViews(items: MutableList<BatchedDeclarationCheckpoint>): MutableList<BatchedDeclarationCheckpointView> {
    val views = mutableListOf<BatchedDeclarationCheckpointView>()
    for (item in items) {
        views.add(toBatchedDeclarationCheckpointView(item))
    }
    return views
}

fun sumBatchedDeclarationCheckpointView(view: BatchedDeclarationCheckpointView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedLimit.toDouble() + parsedFloorValue + parsedAmount
}
