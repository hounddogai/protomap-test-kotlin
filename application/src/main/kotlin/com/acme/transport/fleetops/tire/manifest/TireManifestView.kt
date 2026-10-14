package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire manifest. */
data class TireManifestView(
    var codeText: String,
    var name: String,
    var fuelLiters: String,
    var etaMinutes: String,
    var unit: String,
    var odometer: String,
    var endsAtText: String,
    var channel: String,
    var titleText: String,
    var pinned: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toTireManifestView(item: TireManifest): TireManifestView {
    return TireManifestView(
        codeText = item.code,
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        etaMinutes = item.etaMinutes.toString(),
        unit = truncate(item.unit, 80),
        odometer = item.odometer.toString(),
        endsAtText = item.endsAt.toString(),
        channel = item.channel,
        titleText = item.title,
        pinned = "${item.pinned}",
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireManifestViews(items: MutableList<TireManifest>): MutableList<TireManifestView> {
    val views = mutableListOf<TireManifestView>()
    for (item in items) {
        views.add(toTireManifestView(item))
    }
    return views
}

fun sumTireManifestView(view: TireManifestView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedFuelLiters + parsedEtaMinutes.toDouble() + parsedOdometer.toDouble()
}
