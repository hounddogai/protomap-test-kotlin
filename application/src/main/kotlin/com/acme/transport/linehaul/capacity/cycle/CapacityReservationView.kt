package com.acme.transport.linehaul.capacity.cycle

/** How screens and exports show a capacity reservation. */
data class CapacityReservationView(
    var code: String,
    var name: String,
    var driverIdText: String,
    var endsAtText: String,
    var etaMinutes: String,
    var amountText: String,
    var graceDays: String,
    var approved: String,
    var capacityText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toCapacityReservationView(item: CapacityReservation): CapacityReservationView {
    return CapacityReservationView(
        code = item.code,
        name = item.name,
        driverIdText = item.driverId,
        endsAtText = item.endsAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        amountText = "%.2f".format(item.amount),
        graceDays = item.graceDays.toString(),
        approved = "${item.approved}",
        capacityText = item.capacity.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityReservationViews(items: MutableList<CapacityReservation>): MutableList<CapacityReservationView> {
    val views = mutableListOf<CapacityReservationView>()
    for (item in items) {
        views.add(toCapacityReservationView(item))
    }
    return views
}

fun sumCapacityReservationView(view: CapacityReservationView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedEndsAt.toDouble() + parsedEtaMinutes.toDouble() + parsedAmount
}
