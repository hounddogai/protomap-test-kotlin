package com.acme.transport.freightpay.ratecard.summary

/** How screens and exports show a rate card reservation. */
data class RateCardReservationView(
    var codeText: String,
    var name: String,
    var ratio: String,
    var expiresAt: String,
    var routeCodeText: String,
    var retries: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toRateCardReservationView(item: RateCardReservation): RateCardReservationView {
    return RateCardReservationView(
        codeText = item.code,
        name = item.name,
        ratio = "%.2f".format(item.ratio),
        expiresAt = item.expiresAt.toString(),
        routeCodeText = item.routeCode,
        retries = item.retries.toString(),
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardReservationViews(items: MutableList<RateCardReservation>): MutableList<RateCardReservationView> {
    val views = mutableListOf<RateCardReservationView>()
    for (item in items) {
        views.add(toRateCardReservationView(item))
    }
    return views
}

fun sumRateCardReservationView(view: RateCardReservationView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedRatio + parsedExpiresAt.toDouble() + parsedRetries.toDouble()
}
