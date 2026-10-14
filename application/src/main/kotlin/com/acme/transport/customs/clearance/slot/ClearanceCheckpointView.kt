package com.acme.transport.customs.clearance.slot

/** How screens and exports show a clearance checkpoint. */
data class ClearanceCheckpointView(
    var code: String,
    var nameText: String,
    var description: String,
    var windowDaysText: String,
    var margin: String,
    var weight: String,
    var statusLabel: String,
)

fun toClearanceCheckpointView(item: ClearanceCheckpoint): ClearanceCheckpointView {
    return ClearanceCheckpointView(
        code = item.code,
        nameText = item.name,
        description = item.description,
        windowDaysText = item.windowDays.toString(),
        margin = "%.2f".format(item.margin),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceCheckpointViews(items: MutableList<ClearanceCheckpoint>): MutableList<ClearanceCheckpointView> {
    val views = mutableListOf<ClearanceCheckpointView>()
    for (item in items) {
        views.add(toClearanceCheckpointView(item))
    }
    return views
}

fun sumClearanceCheckpointView(view: ClearanceCheckpointView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedWindowDays.toDouble() + parsedMargin + parsedWeight
}
