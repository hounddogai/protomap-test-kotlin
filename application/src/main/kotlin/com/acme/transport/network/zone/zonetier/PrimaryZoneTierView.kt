package com.acme.transport.network.zone.zonetier

/** How screens and exports show a primary zone tier. */
data class PrimaryZoneTierView(
    var code: String,
    var title: String,
    var batchSize: String,
    var weightText: String,
    var notes: String,
    var windowDays: String,
    var externalRefText: String,
    var limit: String,
    var regionText: String,
    var rateText: String,
    var statusLabel: String,
)

fun toPrimaryZoneTierView(item: PrimaryZoneTier): PrimaryZoneTierView {
    return PrimaryZoneTierView(
        code = item.code,
        title = item.title,
        batchSize = item.batchSize.toString(),
        weightText = "%.2f".format(item.weight),
        notes = item.notes,
        windowDays = item.windowDays.toString(),
        externalRefText = item.externalRef,
        limit = item.limit.toString(),
        regionText = item.region,
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryZoneTierViews(items: MutableList<PrimaryZoneTier>): MutableList<PrimaryZoneTierView> {
    val views = mutableListOf<PrimaryZoneTierView>()
    for (item in items) {
        views.add(toPrimaryZoneTierView(item))
    }
    return views
}

fun sumPrimaryZoneTierView(view: PrimaryZoneTierView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedBatchSize.toDouble() + parsedWeight + parsedWindowDays.toDouble()
}
