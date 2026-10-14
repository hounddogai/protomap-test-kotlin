package com.acme.transport.fleetops.defect.tier

/** How screens and exports show a defect cycle. */
data class DefectCycleView(
    var code: String,
    var label: String,
    var startsAt: String,
    var fuelLiters: String,
    var retries: String,
    var expiresAtText: String,
    var titleText: String,
    var etaMinutes: String,
    var distanceKm: String,
    var externalRef: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toDefectCycleView(item: DefectCycle): DefectCycleView {
    return DefectCycleView(
        code = item.code,
        label = item.label,
        startsAt = item.startsAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        retries = item.retries.toString(),
        expiresAtText = item.expiresAt.toString(),
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        externalRef = item.externalRef,
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectCycleViews(items: MutableList<DefectCycle>): MutableList<DefectCycleView> {
    val views = mutableListOf<DefectCycleView>()
    for (item in items) {
        views.add(toDefectCycleView(item))
    }
    return views
}

fun sumDefectCycleView(view: DefectCycleView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedStartsAt.toDouble() + parsedFuelLiters + parsedRetries.toDouble()
}
