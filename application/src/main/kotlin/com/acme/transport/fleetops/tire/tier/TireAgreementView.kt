package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire agreement. */
data class TireAgreementView(
    var codeText: String,
    var title: String,
    var startsAt: String,
    var archivedText: String,
    var source: String,
    var distanceKmText: String,
    var driverId: String,
    var expiresAtText: String,
    var vehicleId: String,
    var segment: String,
    var statusLabel: String,
)

fun toTireAgreementView(item: TireAgreement): TireAgreementView {
    return TireAgreementView(
        codeText = item.code,
        title = item.title,
        startsAt = item.startsAt.toString(),
        archivedText = "${item.archived}",
        source = item.source,
        distanceKmText = "%.2f".format(item.distanceKm),
        driverId = item.driverId,
        expiresAtText = item.expiresAt.toString(),
        vehicleId = item.vehicleId,
        segment = truncate(item.segment, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireAgreementViews(items: MutableList<TireAgreement>): MutableList<TireAgreementView> {
    val views = mutableListOf<TireAgreementView>()
    for (item in items) {
        views.add(toTireAgreementView(item))
    }
    return views
}

fun sumTireAgreementView(view: TireAgreementView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedStartsAt.toDouble() + parsedDistanceKm + parsedExpiresAt.toDouble()
}
