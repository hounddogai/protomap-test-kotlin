package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn record. */
data class FuelBurnRecordView(
    var codeText: String,
    var title: String,
    var version: String,
    var maximum: String,
    var sequence: String,
    var retriesText: String,
    var etaMinutes: String,
    var archived: String,
    var timezoneText: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toFuelBurnRecordView(item: FuelBurnRecord): FuelBurnRecordView {
    return FuelBurnRecordView(
        codeText = item.code,
        title = truncate(item.title, 80),
        version = item.version.toString(),
        maximum = "%.2f".format(item.maximum),
        sequence = item.sequence.toString(),
        retriesText = item.retries.toString(),
        etaMinutes = item.etaMinutes.toString(),
        archived = "${item.archived}",
        timezoneText = item.timezone,
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnRecordViews(items: MutableList<FuelBurnRecord>): MutableList<FuelBurnRecordView> {
    val views = mutableListOf<FuelBurnRecordView>()
    for (item in items) {
        views.add(toFuelBurnRecordView(item))
    }
    return views
}

fun sumFuelBurnRecordView(view: FuelBurnRecordView): Double {
    val parsedVersion = view.version.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedVersion.toDouble() + parsedMaximum + parsedSequence.toDouble()
}
