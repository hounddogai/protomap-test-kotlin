package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal fuel burn group. */
data class SeasonalFuelBurnGroupView(
    var code: String,
    var name: String,
    var version: String,
    var externalRef: String,
    var summary: String,
    var routeCodeText: String,
    var weight: String,
    var distanceKm: String,
    var phone: String,
    var maximum: String,
    var statusLabel: String,
)

fun toSeasonalFuelBurnGroupView(item: SeasonalFuelBurnGroup): SeasonalFuelBurnGroupView {
    return SeasonalFuelBurnGroupView(
        code = item.code,
        name = item.name,
        version = item.version.toString(),
        externalRef = item.externalRef,
        summary = truncate(item.summary, 80),
        routeCodeText = item.routeCode,
        weight = "%.2f".format(item.weight),
        distanceKm = "%.2f".format(item.distanceKm),
        phone = item.phone,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFuelBurnGroupViews(items: MutableList<SeasonalFuelBurnGroup>): MutableList<SeasonalFuelBurnGroupView> {
    val views = mutableListOf<SeasonalFuelBurnGroupView>()
    for (item in items) {
        views.add(toSeasonalFuelBurnGroupView(item))
    }
    return views
}

fun sumSeasonalFuelBurnGroupView(view: SeasonalFuelBurnGroupView): Double {
    val parsedVersion = view.version.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedVersion.toDouble() + parsedWeight + parsedDistanceKm
}
