package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual qualification profile. */
data class ManualQualificationProfileView(
    var code: String,
    var title: String,
    var startsAtText: String,
    var label: String,
    var active: String,
    var notes: String,
    var routeCode: String,
    var dock: String,
    var vehicleId: String,
    var fuelLiters: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toManualQualificationProfileView(item: ManualQualificationProfile): ManualQualificationProfileView {
    return ManualQualificationProfileView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 80),
        startsAtText = item.startsAt.toString(),
        label = item.label,
        active = "${item.active}",
        notes = item.notes,
        routeCode = item.routeCode,
        dock = truncate(item.dock, 64),
        vehicleId = item.vehicleId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualQualificationProfileViews(items: MutableList<ManualQualificationProfile>): MutableList<ManualQualificationProfileView> {
    val views = mutableListOf<ManualQualificationProfileView>()
    for (item in items) {
        views.add(toManualQualificationProfileView(item))
    }
    return views
}

fun sumManualQualificationProfileView(view: ManualQualificationProfileView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedStartsAt.toDouble() + parsedFuelLiters + parsedStopCount.toDouble()
}
