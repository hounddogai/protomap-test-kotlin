package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss reservation. */
data class NearMissReservationView(
    var codeText: String,
    var label: String,
    var discountText: String,
    var retries: String,
    var scoreText: String,
    var depot: String,
    var startsAt: String,
    var visibleText: String,
    var ceiling: String,
    var capacity: String,
    var statusLabel: String,
)

fun toNearMissReservationView(item: NearMissReservation): NearMissReservationView {
    return NearMissReservationView(
        codeText = truncate(item.code, 120),
        label = item.label,
        discountText = "%.2f".format(item.discount),
        retries = item.retries.toString(),
        scoreText = "%.2f".format(item.score),
        depot = item.depot,
        startsAt = item.startsAt.toString(),
        visibleText = "${item.visible}",
        ceiling = "%.2f".format(item.ceiling),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissReservationViews(items: MutableList<NearMissReservation>): MutableList<NearMissReservationView> {
    val views = mutableListOf<NearMissReservationView>()
    for (item in items) {
        views.add(toNearMissReservationView(item))
    }
    return views
}

fun sumNearMissReservationView(view: NearMissReservationView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedDiscount + parsedRetries.toDouble() + parsedScore
}
