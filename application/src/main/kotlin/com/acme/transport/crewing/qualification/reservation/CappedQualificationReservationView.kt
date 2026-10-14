package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped qualification reservation. */
data class CappedQualificationReservationView(
    var code: String,
    var title: String,
    var amount: String,
    var rate: String,
    var batchSize: String,
    var retriesText: String,
    var timezone: String,
    var countryText: String,
    var statusLabel: String,
)

fun toCappedQualificationReservationView(item: CappedQualificationReservation): CappedQualificationReservationView {
    return CappedQualificationReservationView(
        code = item.code,
        title = truncate(item.title, 120),
        amount = "%.2f".format(item.amount),
        rate = "%.2f".format(item.rate),
        batchSize = item.batchSize.toString(),
        retriesText = item.retries.toString(),
        timezone = truncate(item.timezone, 64),
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedQualificationReservationViews(items: MutableList<CappedQualificationReservation>): MutableList<CappedQualificationReservationView> {
    val views = mutableListOf<CappedQualificationReservationView>()
    for (item in items) {
        views.add(toCappedQualificationReservationView(item))
    }
    return views
}

fun sumCappedQualificationReservationView(view: CappedQualificationReservationView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedAmount + parsedRate + parsedBatchSize.toDouble()
}
