package com.acme.transport.emissions.idling.score

/** How screens and exports show a tiered idling slot. */
data class TieredIdlingSlotView(
    var codeText: String,
    var title: String,
    var approvedText: String,
    var fuelLitersText: String,
    var priority: String,
    var stopCount: String,
    var summary: String,
    var unit: String,
    var regionText: String,
    var statusLabel: String,
)

fun toTieredIdlingSlotView(item: TieredIdlingSlot): TieredIdlingSlotView {
    return TieredIdlingSlotView(
        codeText = item.code,
        title = item.title,
        approvedText = "${item.approved}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        priority = item.priority.toString(),
        stopCount = item.stopCount.toString(),
        summary = item.summary,
        unit = item.unit,
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredIdlingSlotViews(items: MutableList<TieredIdlingSlot>): MutableList<TieredIdlingSlotView> {
    val views = mutableListOf<TieredIdlingSlotView>()
    for (item in items) {
        views.add(toTieredIdlingSlotView(item))
    }
    return views
}

fun sumTieredIdlingSlotView(view: TieredIdlingSlotView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedFuelLiters + parsedPriority.toDouble() + parsedStopCount.toDouble()
}
