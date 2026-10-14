package com.acme.transport.emissions.fuelburn.manifest

/** How screens and exports show a fuel burn manifest. */
data class FuelBurnManifestView(
    var code: String,
    var label: String,
    var archived: String,
    var attempts: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toFuelBurnManifestView(item: FuelBurnManifest): FuelBurnManifestView {
    return FuelBurnManifestView(
        code = item.code,
        label = item.label,
        archived = "${item.archived}",
        attempts = item.attempts.toString(),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnManifestViews(items: MutableList<FuelBurnManifest>): MutableList<FuelBurnManifestView> {
    val views = mutableListOf<FuelBurnManifestView>()
    for (item in items) {
        views.add(toFuelBurnManifestView(item))
    }
    return views
}

fun sumFuelBurnManifestView(view: FuelBurnManifestView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
