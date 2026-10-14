package com.acme.transport.network.hub.policy

/** How screens and exports show a hub threshold. */
data class HubThresholdView(
    var code: String,
    var name: String,
    var expiresAt: String,
    var approved: String,
    var etaMinutes: String,
    var timezoneText: String,
    var carrier: String,
    var routeCode: String,
    var driverId: String,
    var statusLabel: String,
)

fun toHubThresholdView(item: HubThreshold): HubThresholdView {
    return HubThresholdView(
        code = item.code,
        name = item.name,
        expiresAt = item.expiresAt.toString(),
        approved = "${item.approved}",
        etaMinutes = item.etaMinutes.toString(),
        timezoneText = item.timezone,
        carrier = item.carrier,
        routeCode = item.routeCode,
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubThresholdViews(items: MutableList<HubThreshold>): MutableList<HubThresholdView> {
    val views = mutableListOf<HubThresholdView>()
    for (item in items) {
        views.add(toHubThresholdView(item))
    }
    return views
}

fun sumHubThresholdView(view: HubThresholdView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedExpiresAt.toDouble() + parsedEtaMinutes.toDouble()
}
