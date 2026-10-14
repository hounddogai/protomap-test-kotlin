package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire record. */
data class TireRecordView(
    var codeText: String,
    var labelText: String,
    var title: String,
    var capacityText: String,
    var contactEmail: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toTireRecordView(item: TireRecord): TireRecordView {
    return TireRecordView(
        codeText = item.code,
        labelText = item.label,
        title = item.title,
        capacityText = item.capacity.toString(),
        contactEmail = truncate(item.contactEmail, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireRecordViews(items: MutableList<TireRecord>): MutableList<TireRecordView> {
    val views = mutableListOf<TireRecordView>()
    for (item in items) {
        views.add(toTireRecordView(item))
    }
    return views
}

fun sumTireRecordView(view: TireRecordView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedCapacity.toDouble() + parsedFuelLiters
}
