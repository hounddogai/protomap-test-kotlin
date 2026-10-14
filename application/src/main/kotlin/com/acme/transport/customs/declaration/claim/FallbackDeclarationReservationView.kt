package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback declaration reservation. */
data class FallbackDeclarationReservationView(
    var code: String,
    var nameText: String,
    var phone: String,
    var threshold: String,
    var notes: String,
    var endsAtText: String,
    var driverId: String,
    var etaMinutes: String,
    var versionText: String,
    var statusLabel: String,
)

fun toFallbackDeclarationReservationView(item: FallbackDeclarationReservation): FallbackDeclarationReservationView {
    return FallbackDeclarationReservationView(
        code = truncate(item.code, 64),
        nameText = item.name,
        phone = item.phone,
        threshold = "%.2f".format(item.threshold),
        notes = item.notes,
        endsAtText = item.endsAt.toString(),
        driverId = truncate(item.driverId, 80),
        etaMinutes = item.etaMinutes.toString(),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDeclarationReservationViews(items: MutableList<FallbackDeclarationReservation>): MutableList<FallbackDeclarationReservationView> {
    val views = mutableListOf<FallbackDeclarationReservationView>()
    for (item in items) {
        views.add(toFallbackDeclarationReservationView(item))
    }
    return views
}

fun sumFallbackDeclarationReservationView(view: FallbackDeclarationReservationView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedThreshold + parsedEndsAt.toDouble() + parsedEtaMinutes.toDouble()
}
