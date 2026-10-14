package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation claim. */
data class SortationClaimView(
    var code: String,
    var label: String,
    var contactEmailText: String,
    var shiftCodeText: String,
    var currency: String,
    var carrier: String,
    var revision: String,
    var notesText: String,
    var durationMinutesText: String,
    var graceDaysText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toSortationClaimView(item: SortationClaim): SortationClaimView {
    return SortationClaimView(
        code = truncate(item.code, 120),
        label = item.label,
        contactEmailText = item.contactEmail,
        shiftCodeText = truncate(item.shiftCode, 80),
        currency = item.currency,
        carrier = item.carrier,
        revision = item.revision.toString(),
        notesText = item.notes,
        durationMinutesText = item.durationMinutes.toString(),
        graceDaysText = item.graceDays.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationClaimViews(items: MutableList<SortationClaim>): MutableList<SortationClaimView> {
    val views = mutableListOf<SortationClaimView>()
    for (item in items) {
        views.add(toSortationClaimView(item))
    }
    return views
}

fun sumSortationClaimView(view: SortationClaimView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedRevision.toDouble() + parsedDurationMinutes.toDouble() + parsedGraceDays.toDouble()
}
