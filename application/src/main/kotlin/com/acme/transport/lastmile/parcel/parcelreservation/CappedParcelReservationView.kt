package com.acme.transport.lastmile.parcel.parcelreservation

/** How screens and exports show a capped parcel reservation. */
data class CappedParcelReservationView(
    var code: String,
    var titleText: String,
    var retries: String,
    var windowDaysText: String,
    var region: String,
    var segment: String,
    var discountText: String,
    var statusLabel: String,
)

fun toCappedParcelReservationView(item: CappedParcelReservation): CappedParcelReservationView {
    return CappedParcelReservationView(
        code = item.code,
        titleText = item.title,
        retries = item.retries.toString(),
        windowDaysText = item.windowDays.toString(),
        region = item.region,
        segment = item.segment,
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedParcelReservationViews(items: MutableList<CappedParcelReservation>): MutableList<CappedParcelReservationView> {
    val views = mutableListOf<CappedParcelReservationView>()
    for (item in items) {
        views.add(toCappedParcelReservationView(item))
    }
    return views
}

fun sumCappedParcelReservationView(view: CappedParcelReservationView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedRetries.toDouble() + parsedWindowDays.toDouble() + parsedDiscount
}
