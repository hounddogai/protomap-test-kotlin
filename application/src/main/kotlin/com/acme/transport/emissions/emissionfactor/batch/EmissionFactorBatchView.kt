package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor batch. */
data class EmissionFactorBatchView(
    var code: String,
    var label: String,
    var region: String,
    var notesText: String,
    var floorValue: String,
    var capacity: String,
    var dockText: String,
    var durationMinutesText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toEmissionFactorBatchView(item: EmissionFactorBatch): EmissionFactorBatchView {
    return EmissionFactorBatchView(
        code = item.code,
        label = truncate(item.label, 120),
        region = item.region,
        notesText = truncate(item.notes, 120),
        floorValue = "%.2f".format(item.floorValue),
        capacity = item.capacity.toString(),
        dockText = item.dock,
        durationMinutesText = item.durationMinutes.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorBatchViews(items: MutableList<EmissionFactorBatch>): MutableList<EmissionFactorBatchView> {
    val views = mutableListOf<EmissionFactorBatchView>()
    for (item in items) {
        views.add(toEmissionFactorBatchView(item))
    }
    return views
}

fun sumEmissionFactorBatchView(view: EmissionFactorBatchView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedFloorValue + parsedCapacity.toDouble() + parsedDurationMinutes.toDouble()
}
