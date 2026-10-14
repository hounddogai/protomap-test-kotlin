package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted dock door profile. */
data class WeightedDockDoorProfileView(
    var codeText: String,
    var titleText: String,
    var routeCodeText: String,
    var capacityText: String,
    var pallets: String,
    var summary: String,
    var locale: String,
    var ratio: String,
    var distanceKm: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toWeightedDockDoorProfileView(item: WeightedDockDoorProfile): WeightedDockDoorProfileView {
    return WeightedDockDoorProfileView(
        codeText = item.code,
        titleText = item.title,
        routeCodeText = item.routeCode,
        capacityText = item.capacity.toString(),
        pallets = item.pallets.toString(),
        summary = item.summary,
        locale = truncate(item.locale, 40),
        ratio = "%.2f".format(item.ratio),
        distanceKm = "%.2f".format(item.distanceKm),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDockDoorProfileViews(items: MutableList<WeightedDockDoorProfile>): MutableList<WeightedDockDoorProfileView> {
    val views = mutableListOf<WeightedDockDoorProfileView>()
    for (item in items) {
        views.add(toWeightedDockDoorProfileView(item))
    }
    return views
}

fun sumWeightedDockDoorProfileView(view: WeightedDockDoorProfileView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedCapacity.toDouble() + parsedPallets.toDouble() + parsedRatio
}
