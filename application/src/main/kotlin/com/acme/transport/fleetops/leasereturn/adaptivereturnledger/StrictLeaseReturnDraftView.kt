package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

/** How screens and exports show a strict lease return draft. */
data class StrictLeaseReturnDraftView(
    var codeText: String,
    var labelText: String,
    var minimumText: String,
    var carrier: String,
    var cost: String,
    var notesText: String,
    var attemptsText: String,
    var version: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toStrictLeaseReturnDraftView(item: StrictLeaseReturnDraft): StrictLeaseReturnDraftView {
    return StrictLeaseReturnDraftView(
        codeText = item.code,
        labelText = item.label,
        minimumText = "%.2f".format(item.minimum),
        carrier = item.carrier,
        cost = "%.2f".format(item.cost),
        notesText = item.notes,
        attemptsText = item.attempts.toString(),
        version = item.version.toString(),
        timezoneText = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictLeaseReturnDraftViews(items: MutableList<StrictLeaseReturnDraft>): MutableList<StrictLeaseReturnDraftView> {
    val views = mutableListOf<StrictLeaseReturnDraftView>()
    for (item in items) {
        views.add(toStrictLeaseReturnDraftView(item))
    }
    return views
}

fun sumStrictLeaseReturnDraftView(view: StrictLeaseReturnDraftView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedMinimum + parsedCost + parsedAttempts.toDouble()
}
