package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict emission factor allocation. */
data class StrictEmissionFactorAllocationView(
    var code: String,
    var title: String,
    var name: String,
    var pallets: String,
    var timezone: String,
    var driverId: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toStrictEmissionFactorAllocationView(item: StrictEmissionFactorAllocation): StrictEmissionFactorAllocationView {
    return StrictEmissionFactorAllocationView(
        code = truncate(item.code, 80),
        title = item.title,
        name = truncate(item.name, 80),
        pallets = item.pallets.toString(),
        timezone = item.timezone,
        driverId = truncate(item.driverId, 64),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictEmissionFactorAllocationViews(items: MutableList<StrictEmissionFactorAllocation>): MutableList<StrictEmissionFactorAllocationView> {
    val views = mutableListOf<StrictEmissionFactorAllocationView>()
    for (item in items) {
        views.add(toStrictEmissionFactorAllocationView(item))
    }
    return views
}

fun sumStrictEmissionFactorAllocationView(view: StrictEmissionFactorAllocationView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedPallets.toDouble() + parsedDurationMinutes.toDouble()
}
