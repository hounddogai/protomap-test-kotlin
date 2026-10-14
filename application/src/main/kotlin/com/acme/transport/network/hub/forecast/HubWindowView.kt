package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub window. */
data class HubWindowView(
    var codeText: String,
    var name: String,
    var descriptionText: String,
    var updatedAt: String,
    var shiftCodeText: String,
    var odometer: String,
    var archived: String,
    var statusLabel: String,
)

fun toHubWindowView(item: HubWindow): HubWindowView {
    return HubWindowView(
        codeText = truncate(item.code, 80),
        name = item.name,
        descriptionText = item.description,
        updatedAt = item.updatedAt.toString(),
        shiftCodeText = item.shiftCode,
        odometer = item.odometer.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubWindowViews(items: MutableList<HubWindow>): MutableList<HubWindowView> {
    val views = mutableListOf<HubWindowView>()
    for (item in items) {
        views.add(toHubWindowView(item))
    }
    return views
}

fun sumHubWindowView(view: HubWindowView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedUpdatedAt.toDouble() + parsedOdometer.toDouble()
}
