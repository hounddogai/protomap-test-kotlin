package com.acme.transport.terminal.sortation.baseline

/** How screens and exports show a sortation baseline. */
data class SortationBaselineView(
    var codeText: String,
    var nameText: String,
    var weight: String,
    var fuelLiters: String,
    var durationMinutes: String,
    var floorValue: String,
    var margin: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toSortationBaselineView(item: SortationBaseline): SortationBaselineView {
    return SortationBaselineView(
        codeText = item.code,
        nameText = item.name,
        weight = "%.2f".format(item.weight),
        fuelLiters = "%.2f".format(item.fuelLiters),
        durationMinutes = item.durationMinutes.toString(),
        floorValue = "%.2f".format(item.floorValue),
        margin = "%.2f".format(item.margin),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationBaselineViews(items: MutableList<SortationBaseline>): MutableList<SortationBaselineView> {
    val views = mutableListOf<SortationBaselineView>()
    for (item in items) {
        views.add(toSortationBaselineView(item))
    }
    return views
}

fun sumSortationBaselineView(view: SortationBaselineView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedWeight + parsedFuelLiters + parsedDurationMinutes.toDouble()
}
