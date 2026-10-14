package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan reservation. */
data class LanePlanReservationView(
    var codeText: String,
    var titleText: String,
    var checksumText: String,
    var discount: String,
    var shiftCode: String,
    var approved: String,
    var rateText: String,
    var reviewer: String,
    var maximumText: String,
    var margin: String,
    var statusLabel: String,
)

fun toLanePlanReservationView(item: LanePlanReservation): LanePlanReservationView {
    return LanePlanReservationView(
        codeText = truncate(item.code, 40),
        titleText = item.title,
        checksumText = truncate(item.checksum, 40),
        discount = "%.2f".format(item.discount),
        shiftCode = item.shiftCode,
        approved = "${item.approved}",
        rateText = "%.2f".format(item.rate),
        reviewer = item.reviewer,
        maximumText = "%.2f".format(item.maximum),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanReservationViews(items: MutableList<LanePlanReservation>): MutableList<LanePlanReservationView> {
    val views = mutableListOf<LanePlanReservationView>()
    for (item in items) {
        views.add(toLanePlanReservationView(item))
    }
    return views
}

fun sumLanePlanReservationView(view: LanePlanReservationView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedRate = view.rateText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedDiscount + parsedRate + parsedMaximum
}
