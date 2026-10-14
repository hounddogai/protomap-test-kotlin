package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time threshold. */
data class TransitTimeThresholdView(
    var code: String,
    var titleText: String,
    var description: String,
    var createdAt: String,
    var capacity: String,
    var fuelLitersText: String,
    var phone: String,
    var label: String,
    var notes: String,
    var statusLabel: String,
)

fun toTransitTimeThresholdView(item: TransitTimeThreshold): TransitTimeThresholdView {
    return TransitTimeThresholdView(
        code = truncate(item.code, 120),
        titleText = item.title,
        description = item.description,
        createdAt = item.createdAt.toString(),
        capacity = item.capacity.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        phone = item.phone,
        label = item.label,
        notes = truncate(item.notes, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeThresholdViews(items: MutableList<TransitTimeThreshold>): MutableList<TransitTimeThresholdView> {
    val views = mutableListOf<TransitTimeThresholdView>()
    for (item in items) {
        views.add(toTransitTimeThresholdView(item))
    }
    return views
}

fun sumTransitTimeThresholdView(view: TransitTimeThresholdView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedCreatedAt.toDouble() + parsedCapacity.toDouble() + parsedFuelLiters
}
