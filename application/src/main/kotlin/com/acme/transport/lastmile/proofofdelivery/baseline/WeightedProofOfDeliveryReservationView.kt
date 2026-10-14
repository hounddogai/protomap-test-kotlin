package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted proof of delivery reservation. */
data class WeightedProofOfDeliveryReservationView(
    var code: String,
    var label: String,
    var notes: String,
    var capacity: String,
    var limit: String,
    var lane: String,
    var statusLabel: String,
)

fun toWeightedProofOfDeliveryReservationView(item: WeightedProofOfDeliveryReservation): WeightedProofOfDeliveryReservationView {
    return WeightedProofOfDeliveryReservationView(
        code = item.code,
        label = item.label,
        notes = item.notes,
        capacity = item.capacity.toString(),
        limit = item.limit.toString(),
        lane = truncate(item.lane, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedProofOfDeliveryReservationViews(items: MutableList<WeightedProofOfDeliveryReservation>): MutableList<WeightedProofOfDeliveryReservationView> {
    val views = mutableListOf<WeightedProofOfDeliveryReservationView>()
    for (item in items) {
        views.add(toWeightedProofOfDeliveryReservationView(item))
    }
    return views
}

fun sumWeightedProofOfDeliveryReservationView(view: WeightedProofOfDeliveryReservationView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedCapacity.toDouble() + parsedLimit.toDouble()
}
