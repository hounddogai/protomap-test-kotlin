package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel claim. */
data class ParcelClaimView(
    var codeText: String,
    var name: String,
    var notes: String,
    var odometer: String,
    var reviewer: String,
    var sequenceText: String,
    var windowDays: String,
    var description: String,
    var label: String,
    var externalRefText: String,
    var summary: String,
    var statusLabel: String,
)

fun toParcelClaimView(item: ParcelClaim): ParcelClaimView {
    return ParcelClaimView(
        codeText = truncate(item.code, 40),
        name = truncate(item.name, 120),
        notes = item.notes,
        odometer = item.odometer.toString(),
        reviewer = truncate(item.reviewer, 64),
        sequenceText = item.sequence.toString(),
        windowDays = item.windowDays.toString(),
        description = truncate(item.description, 40),
        label = item.label,
        externalRefText = truncate(item.externalRef, 64),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelClaimViews(items: MutableList<ParcelClaim>): MutableList<ParcelClaimView> {
    val views = mutableListOf<ParcelClaimView>()
    for (item in items) {
        views.add(toParcelClaimView(item))
    }
    return views
}

fun sumParcelClaimView(view: ParcelClaimView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedOdometer.toDouble() + parsedSequence.toDouble() + parsedWindowDays.toDouble()
}
