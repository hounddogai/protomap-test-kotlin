package com.acme.transport.fleetops.serviceplan.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan profile. */
data class ServicePlanProfileView(
    var code: String,
    var titleText: String,
    var archived: String,
    var pallets: String,
    var etaMinutes: String,
    var currency: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toServicePlanProfileView(item: ServicePlanProfile): ServicePlanProfileView {
    return ServicePlanProfileView(
        code = item.code,
        titleText = truncate(item.title, 40),
        archived = "${item.archived}",
        pallets = item.pallets.toString(),
        etaMinutes = item.etaMinutes.toString(),
        currency = item.currency,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanProfileViews(items: MutableList<ServicePlanProfile>): MutableList<ServicePlanProfileView> {
    val views = mutableListOf<ServicePlanProfileView>()
    for (item in items) {
        views.add(toServicePlanProfileView(item))
    }
    return views
}

fun sumServicePlanProfileView(view: ServicePlanProfileView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedPallets.toDouble() + parsedEtaMinutes.toDouble() + parsedFuelLiters
}
