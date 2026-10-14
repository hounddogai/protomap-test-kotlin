package com.acme.transport.emissions.fuelburn.burnrequest

/** How screens and exports show a batched fuel burn cycle. */
data class BatchedFuelBurnCycleView(
    var code: String,
    var title: String,
    var durationMinutes: String,
    var timezone: String,
    var statusLabel: String,
)

fun toBatchedFuelBurnCycleView(item: BatchedFuelBurnCycle): BatchedFuelBurnCycleView {
    return BatchedFuelBurnCycleView(
        code = item.code,
        title = item.title,
        durationMinutes = item.durationMinutes.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedFuelBurnCycleViews(items: MutableList<BatchedFuelBurnCycle>): MutableList<BatchedFuelBurnCycleView> {
    val views = mutableListOf<BatchedFuelBurnCycleView>()
    for (item in items) {
        views.add(toBatchedFuelBurnCycleView(item))
    }
    return views
}

fun sumBatchedFuelBurnCycleView(view: BatchedFuelBurnCycleView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedDurationMinutes.toDouble()
}
