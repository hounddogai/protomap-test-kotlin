package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual utilization reservation. */
data class ManualUtilizationReservationView(
    var code: String,
    var nameText: String,
    var retriesText: String,
    var minimumText: String,
    var amount: String,
    var category: String,
    var title: String,
    var contactEmail: String,
    var notes: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toManualUtilizationReservationView(item: ManualUtilizationReservation): ManualUtilizationReservationView {
    return ManualUtilizationReservationView(
        code = item.code,
        nameText = item.name,
        retriesText = item.retries.toString(),
        minimumText = "%.2f".format(item.minimum),
        amount = "%.2f".format(item.amount),
        category = truncate(item.category, 64),
        title = item.title,
        contactEmail = item.contactEmail,
        notes = item.notes,
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualUtilizationReservationViews(items: MutableList<ManualUtilizationReservation>): MutableList<ManualUtilizationReservationView> {
    val views = mutableListOf<ManualUtilizationReservationView>()
    for (item in items) {
        views.add(toManualUtilizationReservationView(item))
    }
    return views
}

fun sumManualUtilizationReservationView(view: ManualUtilizationReservationView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedRetries.toDouble() + parsedMinimum + parsedAmount
}
