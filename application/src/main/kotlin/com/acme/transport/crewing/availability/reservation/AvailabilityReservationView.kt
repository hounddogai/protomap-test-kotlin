package com.acme.transport.crewing.availability.reservation

/** How screens and exports show a availability reservation. */
data class AvailabilityReservationView(
    var codeText: String,
    var labelText: String,
    var driverIdText: String,
    var thresholdText: String,
    var ceiling: String,
    var sequenceText: String,
    var distanceKm: String,
    var minimum: String,
    var cost: String,
    var statusLabel: String,
)

fun toAvailabilityReservationView(item: AvailabilityReservation): AvailabilityReservationView {
    return AvailabilityReservationView(
        codeText = item.code,
        labelText = item.label,
        driverIdText = item.driverId,
        thresholdText = "%.2f".format(item.threshold),
        ceiling = "%.2f".format(item.ceiling),
        sequenceText = item.sequence.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        minimum = "%.2f".format(item.minimum),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityReservationViews(items: MutableList<AvailabilityReservation>): MutableList<AvailabilityReservationView> {
    val views = mutableListOf<AvailabilityReservationView>()
    for (item in items) {
        views.add(toAvailabilityReservationView(item))
    }
    return views
}

fun sumAvailabilityReservationView(view: AvailabilityReservationView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedThreshold + parsedCeiling + parsedSequence.toDouble()
}
