package com.acme.transport.network.servicearea.draft

/** How screens and exports show a legacy service area cycle. */
data class LegacyServiceAreaCycleView(
    var codeText: String,
    var title: String,
    var regionText: String,
    var distanceKm: String,
    var segment: String,
    var attempts: String,
    var priorityText: String,
    var visible: String,
    var discount: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toLegacyServiceAreaCycleView(item: LegacyServiceAreaCycle): LegacyServiceAreaCycleView {
    return LegacyServiceAreaCycleView(
        codeText = item.code,
        title = item.title,
        regionText = item.region,
        distanceKm = "%.2f".format(item.distanceKm),
        segment = item.segment,
        attempts = item.attempts.toString(),
        priorityText = item.priority.toString(),
        visible = "${item.visible}",
        discount = "%.2f".format(item.discount),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyServiceAreaCycleViews(items: MutableList<LegacyServiceAreaCycle>): MutableList<LegacyServiceAreaCycleView> {
    val views = mutableListOf<LegacyServiceAreaCycleView>()
    for (item in items) {
        views.add(toLegacyServiceAreaCycleView(item))
    }
    return views
}

fun sumLegacyServiceAreaCycleView(view: LegacyServiceAreaCycleView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedDistanceKm + parsedAttempts.toDouble() + parsedPriority.toDouble()
}
