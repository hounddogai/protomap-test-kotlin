package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled defect profile. */
data class ScheduledDefectProfileView(
    var code: String,
    var nameText: String,
    var loadKg: String,
    var externalRefText: String,
    var rateText: String,
    var distanceKm: String,
    var unit: String,
    var driverId: String,
    var odometer: String,
    var statusLabel: String,
)

fun toScheduledDefectProfileView(item: ScheduledDefectProfile): ScheduledDefectProfileView {
    return ScheduledDefectProfileView(
        code = truncate(item.code, 64),
        nameText = item.name,
        loadKg = "%.2f".format(item.loadKg),
        externalRefText = item.externalRef,
        rateText = "%.2f".format(item.rate),
        distanceKm = "%.2f".format(item.distanceKm),
        unit = item.unit,
        driverId = truncate(item.driverId, 80),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDefectProfileViews(items: MutableList<ScheduledDefectProfile>): MutableList<ScheduledDefectProfileView> {
    val views = mutableListOf<ScheduledDefectProfileView>()
    for (item in items) {
        views.add(toScheduledDefectProfileView(item))
    }
    return views
}

fun sumScheduledDefectProfileView(view: ScheduledDefectProfileView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRate = view.rateText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedLoadKg + parsedRate + parsedDistanceKm
}
