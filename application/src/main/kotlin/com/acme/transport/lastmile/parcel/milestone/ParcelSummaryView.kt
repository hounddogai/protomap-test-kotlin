package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel summary. */
data class ParcelSummaryView(
    var codeText: String,
    var title: String,
    var amount: String,
    var loadKg: String,
    var laneText: String,
    var revisionText: String,
    var maximum: String,
    var distanceKm: String,
    var shiftCode: String,
    var odometer: String,
    var statusLabel: String,
)

fun toParcelSummaryView(item: ParcelSummary): ParcelSummaryView {
    return ParcelSummaryView(
        codeText = item.code,
        title = item.title,
        amount = "%.2f".format(item.amount),
        loadKg = "%.2f".format(item.loadKg),
        laneText = truncate(item.lane, 80),
        revisionText = item.revision.toString(),
        maximum = "%.2f".format(item.maximum),
        distanceKm = "%.2f".format(item.distanceKm),
        shiftCode = truncate(item.shiftCode, 80),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelSummaryViews(items: MutableList<ParcelSummary>): MutableList<ParcelSummaryView> {
    val views = mutableListOf<ParcelSummaryView>()
    for (item in items) {
        views.add(toParcelSummaryView(item))
    }
    return views
}

fun sumParcelSummaryView(view: ParcelSummaryView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedAmount + parsedLoadKg + parsedRevision.toDouble()
}
