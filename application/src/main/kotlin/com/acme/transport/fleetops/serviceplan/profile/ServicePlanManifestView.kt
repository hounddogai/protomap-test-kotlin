package com.acme.transport.fleetops.serviceplan.profile

/** How screens and exports show a service plan manifest. */
data class ServicePlanManifestView(
    var code: String,
    var name: String,
    var visibleText: String,
    var palletsText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toServicePlanManifestView(item: ServicePlanManifest): ServicePlanManifestView {
    return ServicePlanManifestView(
        code = item.code,
        name = item.name,
        visibleText = "${item.visible}",
        palletsText = item.pallets.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanManifestViews(items: MutableList<ServicePlanManifest>): MutableList<ServicePlanManifestView> {
    val views = mutableListOf<ServicePlanManifestView>()
    for (item in items) {
        views.add(toServicePlanManifestView(item))
    }
    return views
}

fun sumServicePlanManifestView(view: ServicePlanManifestView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedPallets.toDouble() + parsedDurationMinutes.toDouble()
}
