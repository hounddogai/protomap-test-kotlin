package com.acme.transport.crewing.dutyperiod.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period reservation. */
data class DutyPeriodReservationView(
    var code: String,
    var title: String,
    var weight: String,
    var country: String,
    var discount: String,
    var retries: String,
    var timezoneText: String,
    var score: String,
    var statusLabel: String,
)

fun toDutyPeriodReservationView(item: DutyPeriodReservation): DutyPeriodReservationView {
    return DutyPeriodReservationView(
        code = truncate(item.code, 40),
        title = item.title,
        weight = "%.2f".format(item.weight),
        country = truncate(item.country, 80),
        discount = "%.2f".format(item.discount),
        retries = item.retries.toString(),
        timezoneText = truncate(item.timezone, 40),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodReservationViews(items: MutableList<DutyPeriodReservation>): MutableList<DutyPeriodReservationView> {
    val views = mutableListOf<DutyPeriodReservationView>()
    for (item in items) {
        views.add(toDutyPeriodReservationView(item))
    }
    return views
}

fun sumDutyPeriodReservationView(view: DutyPeriodReservationView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedWeight + parsedDiscount + parsedRetries.toDouble()
}
