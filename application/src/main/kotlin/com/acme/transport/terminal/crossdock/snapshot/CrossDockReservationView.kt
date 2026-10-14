package com.acme.transport.terminal.crossdock.snapshot

/** How screens and exports show a cross dock reservation. */
data class CrossDockReservationView(
    var codeText: String,
    var titleText: String,
    var depot: String,
    var discountText: String,
    var source: String,
    var retries: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toCrossDockReservationView(item: CrossDockReservation): CrossDockReservationView {
    return CrossDockReservationView(
        codeText = item.code,
        titleText = item.title,
        depot = item.depot,
        discountText = "%.2f".format(item.discount),
        source = item.source,
        retries = item.retries.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockReservationViews(items: MutableList<CrossDockReservation>): MutableList<CrossDockReservationView> {
    val views = mutableListOf<CrossDockReservationView>()
    for (item in items) {
        views.add(toCrossDockReservationView(item))
    }
    return views
}

fun sumCrossDockReservationView(view: CrossDockReservationView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedDiscount + parsedRetries.toDouble() + parsedCeiling
}
