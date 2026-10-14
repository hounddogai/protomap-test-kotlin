package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling zone window. */
data class RollingZoneWindowView(
    var codeText: String,
    var title: String,
    var limit: String,
    var minimum: String,
    var quantity: String,
    var capacityText: String,
    var contactEmail: String,
    var margin: String,
    var updatedAtText: String,
    var externalRef: String,
    var revision: String,
    var statusLabel: String,
)

fun toRollingZoneWindowView(item: RollingZoneWindow): RollingZoneWindowView {
    return RollingZoneWindowView(
        codeText = truncate(item.code, 80),
        title = truncate(item.title, 64),
        limit = item.limit.toString(),
        minimum = "%.2f".format(item.minimum),
        quantity = item.quantity.toString(),
        capacityText = item.capacity.toString(),
        contactEmail = item.contactEmail,
        margin = "%.2f".format(item.margin),
        updatedAtText = item.updatedAt.toString(),
        externalRef = truncate(item.externalRef, 64),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingZoneWindowViews(items: MutableList<RollingZoneWindow>): MutableList<RollingZoneWindowView> {
    val views = mutableListOf<RollingZoneWindowView>()
    for (item in items) {
        views.add(toRollingZoneWindowView(item))
    }
    return views
}

fun sumRollingZoneWindowView(view: RollingZoneWindowView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedLimit.toDouble() + parsedMinimum + parsedQuantity.toDouble()
}
