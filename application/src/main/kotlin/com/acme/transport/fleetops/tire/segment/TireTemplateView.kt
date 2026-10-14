package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire template. */
data class TireTemplateView(
    var code: String,
    var name: String,
    var capacityText: String,
    var descriptionText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toTireTemplateView(item: TireTemplate): TireTemplateView {
    return TireTemplateView(
        code = item.code,
        name = item.name,
        capacityText = item.capacity.toString(),
        descriptionText = truncate(item.description, 40),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireTemplateViews(items: MutableList<TireTemplate>): MutableList<TireTemplateView> {
    val views = mutableListOf<TireTemplateView>()
    for (item in items) {
        views.add(toTireTemplateView(item))
    }
    return views
}

fun sumTireTemplateView(view: TireTemplateView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedCapacity.toDouble() + parsedStartsAt.toDouble()
}
