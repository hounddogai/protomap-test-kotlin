package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional fuel burn target. */
data class RegionalFuelBurnTargetView(
    var code: String,
    var title: String,
    var vehicleId: String,
    var pallets: String,
    var versionText: String,
    var countryText: String,
    var trailerIdText: String,
    var routeCode: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toRegionalFuelBurnTargetView(item: RegionalFuelBurnTarget): RegionalFuelBurnTargetView {
    return RegionalFuelBurnTargetView(
        code = item.code,
        title = truncate(item.title, 120),
        vehicleId = item.vehicleId,
        pallets = item.pallets.toString(),
        versionText = item.version.toString(),
        countryText = item.country,
        trailerIdText = item.trailerId,
        routeCode = truncate(item.routeCode, 64),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalFuelBurnTargetViews(items: MutableList<RegionalFuelBurnTarget>): MutableList<RegionalFuelBurnTargetView> {
    val views = mutableListOf<RegionalFuelBurnTargetView>()
    for (item in items) {
        views.add(toRegionalFuelBurnTargetView(item))
    }
    return views
}

fun sumRegionalFuelBurnTargetView(view: RegionalFuelBurnTargetView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedPallets.toDouble() + parsedVersion.toDouble() + parsedWindowDays.toDouble()
}
