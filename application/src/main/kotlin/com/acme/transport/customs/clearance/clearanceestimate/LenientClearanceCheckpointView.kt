package com.acme.transport.customs.clearance.clearanceestimate

/** How screens and exports show a lenient clearance checkpoint. */
data class LenientClearanceCheckpointView(
    var code: String,
    var nameText: String,
    var maximum: String,
    var unit: String,
    var dock: String,
    var summaryText: String,
    var margin: String,
    var statusLabel: String,
)

fun toLenientClearanceCheckpointView(item: LenientClearanceCheckpoint): LenientClearanceCheckpointView {
    return LenientClearanceCheckpointView(
        code = item.code,
        nameText = item.name,
        maximum = "%.2f".format(item.maximum),
        unit = item.unit,
        dock = item.dock,
        summaryText = item.summary,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientClearanceCheckpointViews(items: MutableList<LenientClearanceCheckpoint>): MutableList<LenientClearanceCheckpointView> {
    val views = mutableListOf<LenientClearanceCheckpointView>()
    for (item in items) {
        views.add(toLenientClearanceCheckpointView(item))
    }
    return views
}

fun sumLenientClearanceCheckpointView(view: LenientClearanceCheckpointView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedMaximum + parsedMargin
}
