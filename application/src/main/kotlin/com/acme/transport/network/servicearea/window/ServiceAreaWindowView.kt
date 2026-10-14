package com.acme.transport.network.servicearea.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area window. */
data class ServiceAreaWindowView(
    var code: String,
    var title: String,
    var timezoneText: String,
    var vehicleId: String,
    var source: String,
    var region: String,
    var shiftCodeText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toServiceAreaWindowView(item: ServiceAreaWindow): ServiceAreaWindowView {
    return ServiceAreaWindowView(
        code = truncate(item.code, 64),
        title = item.title,
        timezoneText = truncate(item.timezone, 64),
        vehicleId = item.vehicleId,
        source = item.source,
        region = truncate(item.region, 80),
        shiftCodeText = item.shiftCode,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaWindowViews(items: MutableList<ServiceAreaWindow>): MutableList<ServiceAreaWindowView> {
    val views = mutableListOf<ServiceAreaWindowView>()
    for (item in items) {
        views.add(toServiceAreaWindowView(item))
    }
    return views
}

fun sumServiceAreaWindowView(view: ServiceAreaWindowView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
