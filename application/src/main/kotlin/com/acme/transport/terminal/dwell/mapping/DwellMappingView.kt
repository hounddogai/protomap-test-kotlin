package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell mapping. */
data class DwellMappingView(
    var code: String,
    var title: String,
    var fuelLitersText: String,
    var durationMinutesText: String,
    var label: String,
    var odometer: String,
    var notesText: String,
    var phone: String,
    var depot: String,
    var archived: String,
    var statusLabel: String,
)

fun toDwellMappingView(item: DwellMapping): DwellMappingView {
    return DwellMappingView(
        code = item.code,
        title = item.title,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        durationMinutesText = item.durationMinutes.toString(),
        label = item.label,
        odometer = item.odometer.toString(),
        notesText = truncate(item.notes, 64),
        phone = truncate(item.phone, 64),
        depot = item.depot,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellMappingViews(items: MutableList<DwellMapping>): MutableList<DwellMappingView> {
    val views = mutableListOf<DwellMappingView>()
    for (item in items) {
        views.add(toDwellMappingView(item))
    }
    return views
}

fun sumDwellMappingView(view: DwellMappingView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedFuelLiters + parsedDurationMinutes.toDouble() + parsedOdometer.toDouble()
}
