package com.acme.transport.terminal.dockdoor.threshold

/** How screens and exports show a batched dock door record. */
data class BatchedDockDoorRecordView(
    var codeText: String,
    var title: String,
    var label: String,
    var activeText: String,
    var country: String,
    var ownerId: String,
    var fuelLitersText: String,
    var retries: String,
    var margin: String,
    var statusLabel: String,
)

fun toBatchedDockDoorRecordView(item: BatchedDockDoorRecord): BatchedDockDoorRecordView {
    return BatchedDockDoorRecordView(
        codeText = item.code,
        title = item.title,
        label = item.label,
        activeText = "${item.active}",
        country = item.country,
        ownerId = item.ownerId,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        retries = item.retries.toString(),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDockDoorRecordViews(items: MutableList<BatchedDockDoorRecord>): MutableList<BatchedDockDoorRecordView> {
    val views = mutableListOf<BatchedDockDoorRecordView>()
    for (item in items) {
        views.add(toBatchedDockDoorRecordView(item))
    }
    return views
}

fun sumBatchedDockDoorRecordView(view: BatchedDockDoorRecordView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedFuelLiters + parsedRetries.toDouble() + parsedMargin
}
