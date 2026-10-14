package com.acme.transport.linehaul.capacity.allocation

/** How screens and exports show a seasonal capacity slot. */
data class SeasonalCapacitySlotView(
    var code: String,
    var labelText: String,
    var carrier: String,
    var distanceKm: String,
    var region: String,
    var statusLabel: String,
)

fun toSeasonalCapacitySlotView(item: SeasonalCapacitySlot): SeasonalCapacitySlotView {
    return SeasonalCapacitySlotView(
        code = item.code,
        labelText = item.label,
        carrier = item.carrier,
        distanceKm = "%.2f".format(item.distanceKm),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCapacitySlotViews(items: MutableList<SeasonalCapacitySlot>): MutableList<SeasonalCapacitySlotView> {
    val views = mutableListOf<SeasonalCapacitySlotView>()
    for (item in items) {
        views.add(toSeasonalCapacitySlotView(item))
    }
    return views
}

fun sumSeasonalCapacitySlotView(view: SeasonalCapacitySlotView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedDistanceKm
}
