package com.acme.transport.emissions.evcharge.chargescore

/** How screens and exports show a ev charge schedule. */
data class EvChargeScheduleView(
    var code: String,
    var label: String,
    var ceiling: String,
    var distanceKm: String,
    var expiresAt: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toEvChargeScheduleView(item: EvChargeSchedule): EvChargeScheduleView {
    return EvChargeScheduleView(
        code = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        distanceKm = "%.2f".format(item.distanceKm),
        expiresAt = item.expiresAt.toString(),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeScheduleViews(items: MutableList<EvChargeSchedule>): MutableList<EvChargeScheduleView> {
    val views = mutableListOf<EvChargeScheduleView>()
    for (item in items) {
        views.add(toEvChargeScheduleView(item))
    }
    return views
}

fun sumEvChargeScheduleView(view: EvChargeScheduleView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedCeiling + parsedDistanceKm + parsedExpiresAt.toDouble()
}
