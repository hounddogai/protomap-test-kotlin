package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan reservation. */
data class ServicePlanReservationView(
    var code: String,
    var nameText: String,
    var marginText: String,
    var amount: String,
    var durationMinutes: String,
    var batchSize: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toServicePlanReservationView(item: ServicePlanReservation): ServicePlanReservationView {
    return ServicePlanReservationView(
        code = truncate(item.code, 40),
        nameText = item.name,
        marginText = "%.2f".format(item.margin),
        amount = "%.2f".format(item.amount),
        durationMinutes = item.durationMinutes.toString(),
        batchSize = item.batchSize.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanReservationViews(items: MutableList<ServicePlanReservation>): MutableList<ServicePlanReservationView> {
    val views = mutableListOf<ServicePlanReservationView>()
    for (item in items) {
        views.add(toServicePlanReservationView(item))
    }
    return views
}

fun sumServicePlanReservationView(view: ServicePlanReservationView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedMargin + parsedAmount + parsedDurationMinutes.toDouble()
}
