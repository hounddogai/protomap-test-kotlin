package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy inspection schedule. */
data class LegacyInspectionScheduleView(
    var code: String,
    var name: String,
    var distanceKmText: String,
    var phone: String,
    var pallets: String,
    var etaMinutes: String,
    var unit: String,
    var statusLabel: String,
)

fun toLegacyInspectionScheduleView(item: LegacyInspectionSchedule): LegacyInspectionScheduleView {
    return LegacyInspectionScheduleView(
        code = item.code,
        name = item.name,
        distanceKmText = "%.2f".format(item.distanceKm),
        phone = item.phone,
        pallets = item.pallets.toString(),
        etaMinutes = item.etaMinutes.toString(),
        unit = truncate(item.unit, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionScheduleViews(items: MutableList<LegacyInspectionSchedule>): MutableList<LegacyInspectionScheduleView> {
    val views = mutableListOf<LegacyInspectionScheduleView>()
    for (item in items) {
        views.add(toLegacyInspectionScheduleView(item))
    }
    return views
}

fun sumLegacyInspectionScheduleView(view: LegacyInspectionScheduleView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedDistanceKm + parsedPallets.toDouble() + parsedEtaMinutes.toDouble()
}
