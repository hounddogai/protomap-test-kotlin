package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy emission factor manifest. */
data class LegacyEmissionFactorManifestView(
    var code: String,
    var name: String,
    var notes: String,
    var title: String,
    var visible: String,
    var approved: String,
    var description: String,
    var fuelLiters: String,
    var vehicleId: String,
    var priority: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toLegacyEmissionFactorManifestView(item: LegacyEmissionFactorManifest): LegacyEmissionFactorManifestView {
    return LegacyEmissionFactorManifestView(
        code = item.code,
        name = item.name,
        notes = item.notes,
        title = truncate(item.title, 40),
        visible = "${item.visible}",
        approved = "${item.approved}",
        description = item.description,
        fuelLiters = "%.2f".format(item.fuelLiters),
        vehicleId = item.vehicleId,
        priority = item.priority.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyEmissionFactorManifestViews(items: MutableList<LegacyEmissionFactorManifest>): MutableList<LegacyEmissionFactorManifestView> {
    val views = mutableListOf<LegacyEmissionFactorManifestView>()
    for (item in items) {
        views.add(toLegacyEmissionFactorManifestView(item))
    }
    return views
}

fun sumLegacyEmissionFactorManifestView(view: LegacyEmissionFactorManifestView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedFuelLiters + parsedPriority.toDouble() + parsedWindowDays.toDouble()
}
