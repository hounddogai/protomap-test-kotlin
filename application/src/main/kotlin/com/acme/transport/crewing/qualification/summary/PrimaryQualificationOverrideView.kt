package com.acme.transport.crewing.qualification.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary qualification override. */
data class PrimaryQualificationOverrideView(
    var code: String,
    var label: String,
    var ceiling: String,
    var ratioText: String,
    var fuelLiters: String,
    var trailerIdText: String,
    var loadKg: String,
    var vehicleId: String,
    var maximum: String,
    var quantity: String,
    var statusLabel: String,
)

fun toPrimaryQualificationOverrideView(item: PrimaryQualificationOverride): PrimaryQualificationOverrideView {
    return PrimaryQualificationOverrideView(
        code = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        ratioText = "%.2f".format(item.ratio),
        fuelLiters = "%.2f".format(item.fuelLiters),
        trailerIdText = truncate(item.trailerId, 64),
        loadKg = "%.2f".format(item.loadKg),
        vehicleId = item.vehicleId,
        maximum = "%.2f".format(item.maximum),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryQualificationOverrideViews(items: MutableList<PrimaryQualificationOverride>): MutableList<PrimaryQualificationOverrideView> {
    val views = mutableListOf<PrimaryQualificationOverrideView>()
    for (item in items) {
        views.add(toPrimaryQualificationOverrideView(item))
    }
    return views
}

fun sumPrimaryQualificationOverrideView(view: PrimaryQualificationOverrideView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedCeiling + parsedRatio + parsedFuelLiters
}
