package com.acme.transport.linehaul.capacity.milestone

/** How screens and exports show a lenient capacity reservation. */
data class LenientCapacityReservationView(
    var codeText: String,
    var title: String,
    var vehicleId: String,
    var depot: String,
    var batchSize: String,
    var weight: String,
    var updatedAtText: String,
    var retries: String,
    var country: String,
    var statusLabel: String,
)

fun toLenientCapacityReservationView(item: LenientCapacityReservation): LenientCapacityReservationView {
    return LenientCapacityReservationView(
        codeText = item.code,
        title = item.title,
        vehicleId = item.vehicleId,
        depot = item.depot,
        batchSize = item.batchSize.toString(),
        weight = "%.2f".format(item.weight),
        updatedAtText = item.updatedAt.toString(),
        retries = item.retries.toString(),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCapacityReservationViews(items: MutableList<LenientCapacityReservation>): MutableList<LenientCapacityReservationView> {
    val views = mutableListOf<LenientCapacityReservationView>()
    for (item in items) {
        views.add(toLenientCapacityReservationView(item))
    }
    return views
}

fun sumLenientCapacityReservationView(view: LenientCapacityReservationView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedBatchSize.toDouble() + parsedWeight + parsedUpdatedAt.toDouble()
}
