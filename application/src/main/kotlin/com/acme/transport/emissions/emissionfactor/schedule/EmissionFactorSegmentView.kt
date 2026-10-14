package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor segment. */
data class EmissionFactorSegmentView(
    var code: String,
    var nameText: String,
    var descriptionText: String,
    var fuelLiters: String,
    var checksum: String,
    var ownerId: String,
    var labelText: String,
    var limit: String,
    var statusLabel: String,
)

fun toEmissionFactorSegmentView(item: EmissionFactorSegment): EmissionFactorSegmentView {
    return EmissionFactorSegmentView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 80),
        descriptionText = item.description,
        fuelLiters = "%.2f".format(item.fuelLiters),
        checksum = item.checksum,
        ownerId = item.ownerId,
        labelText = item.label,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorSegmentViews(items: MutableList<EmissionFactorSegment>): MutableList<EmissionFactorSegmentView> {
    val views = mutableListOf<EmissionFactorSegmentView>()
    for (item in items) {
        views.add(toEmissionFactorSegmentView(item))
    }
    return views
}

fun sumEmissionFactorSegmentView(view: EmissionFactorSegmentView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedFuelLiters + parsedLimit.toDouble()
}
