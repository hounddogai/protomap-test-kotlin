package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor reservation. */
data class EmissionFactorReservationView(
    var codeText: String,
    var title: String,
    var margin: String,
    var costText: String,
    var sequence: String,
    var regionText: String,
    var ownerId: String,
    var visible: String,
    var active: String,
    var phone: String,
    var priority: String,
    var statusLabel: String,
)

fun toEmissionFactorReservationView(item: EmissionFactorReservation): EmissionFactorReservationView {
    return EmissionFactorReservationView(
        codeText = item.code,
        title = truncate(item.title, 120),
        margin = "%.2f".format(item.margin),
        costText = "%.2f".format(item.cost),
        sequence = item.sequence.toString(),
        regionText = item.region,
        ownerId = truncate(item.ownerId, 120),
        visible = "${item.visible}",
        active = "${item.active}",
        phone = truncate(item.phone, 40),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorReservationViews(items: MutableList<EmissionFactorReservation>): MutableList<EmissionFactorReservationView> {
    val views = mutableListOf<EmissionFactorReservationView>()
    for (item in items) {
        views.add(toEmissionFactorReservationView(item))
    }
    return views
}

fun sumEmissionFactorReservationView(view: EmissionFactorReservationView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedCost = view.costText.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedMargin + parsedCost + parsedSequence.toDouble()
}
