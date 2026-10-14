package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted ev charge mapping. */
data class WeightedEvChargeMappingView(
    var codeText: String,
    var name: String,
    var distanceKmText: String,
    var capacityText: String,
    var pinned: String,
    var vehicleId: String,
    var endsAtText: String,
    var limit: String,
    var statusLabel: String,
)

fun toWeightedEvChargeMappingView(item: WeightedEvChargeMapping): WeightedEvChargeMappingView {
    return WeightedEvChargeMappingView(
        codeText = item.code,
        name = truncate(item.name, 80),
        distanceKmText = "%.2f".format(item.distanceKm),
        capacityText = item.capacity.toString(),
        pinned = "${item.pinned}",
        vehicleId = item.vehicleId,
        endsAtText = item.endsAt.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedEvChargeMappingViews(items: MutableList<WeightedEvChargeMapping>): MutableList<WeightedEvChargeMappingView> {
    val views = mutableListOf<WeightedEvChargeMappingView>()
    for (item in items) {
        views.add(toWeightedEvChargeMappingView(item))
    }
    return views
}

fun sumWeightedEvChargeMappingView(view: WeightedEvChargeMappingView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedDistanceKm + parsedCapacity.toDouble() + parsedEndsAt.toDouble()
}
