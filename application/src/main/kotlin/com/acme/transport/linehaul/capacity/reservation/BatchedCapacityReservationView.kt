package com.acme.transport.linehaul.capacity.reservation

/** How screens and exports show a batched capacity reservation. */
data class BatchedCapacityReservationView(
    var code: String,
    var title: String,
    var threshold: String,
    var segmentText: String,
    var carrier: String,
    var version: String,
    var routeCode: String,
    var depot: String,
    var name: String,
    var statusLabel: String,
)

fun toBatchedCapacityReservationView(item: BatchedCapacityReservation): BatchedCapacityReservationView {
    return BatchedCapacityReservationView(
        code = item.code,
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        segmentText = item.segment,
        carrier = item.carrier,
        version = item.version.toString(),
        routeCode = item.routeCode,
        depot = item.depot,
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCapacityReservationViews(items: MutableList<BatchedCapacityReservation>): MutableList<BatchedCapacityReservationView> {
    val views = mutableListOf<BatchedCapacityReservationView>()
    for (item in items) {
        views.add(toBatchedCapacityReservationView(item))
    }
    return views
}

fun sumBatchedCapacityReservationView(view: BatchedCapacityReservationView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedThreshold + parsedVersion.toDouble()
}
