package com.acme.transport.freightpay.dispute.request

/** How screens and exports show a batched dispute reservation. */
data class BatchedDisputeReservationView(
    var codeText: String,
    var title: String,
    var limit: String,
    var updatedAt: String,
    var ownerId: String,
    var floorValue: String,
    var discount: String,
    var locale: String,
    var minimum: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toBatchedDisputeReservationView(item: BatchedDisputeReservation): BatchedDisputeReservationView {
    return BatchedDisputeReservationView(
        codeText = item.code,
        title = item.title,
        limit = item.limit.toString(),
        updatedAt = item.updatedAt.toString(),
        ownerId = item.ownerId,
        floorValue = "%.2f".format(item.floorValue),
        discount = "%.2f".format(item.discount),
        locale = item.locale,
        minimum = "%.2f".format(item.minimum),
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDisputeReservationViews(items: MutableList<BatchedDisputeReservation>): MutableList<BatchedDisputeReservationView> {
    val views = mutableListOf<BatchedDisputeReservationView>()
    for (item in items) {
        views.add(toBatchedDisputeReservationView(item))
    }
    return views
}

fun sumBatchedDisputeReservationView(view: BatchedDisputeReservationView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedLimit.toDouble() + parsedUpdatedAt.toDouble() + parsedFloorValue
}
