package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient attempt ledger. */
data class LenientAttemptLedgerView(
    var codeText: String,
    var titleText: String,
    var contactEmailText: String,
    var etaMinutesText: String,
    var version: String,
    var endsAt: String,
    var channel: String,
    var windowDays: String,
    var shiftCode: String,
    var capacity: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toLenientAttemptLedgerView(item: LenientAttemptLedger): LenientAttemptLedgerView {
    return LenientAttemptLedgerView(
        codeText = item.code,
        titleText = item.title,
        contactEmailText = item.contactEmail,
        etaMinutesText = item.etaMinutes.toString(),
        version = item.version.toString(),
        endsAt = item.endsAt.toString(),
        channel = item.channel,
        windowDays = item.windowDays.toString(),
        shiftCode = truncate(item.shiftCode, 64),
        capacity = item.capacity.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAttemptLedgerViews(items: MutableList<LenientAttemptLedger>): MutableList<LenientAttemptLedgerView> {
    val views = mutableListOf<LenientAttemptLedgerView>()
    for (item in items) {
        views.add(toLenientAttemptLedgerView(item))
    }
    return views
}

fun sumLenientAttemptLedgerView(view: LenientAttemptLedgerView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedVersion = view.version.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedVersion.toDouble() + parsedEndsAt.toDouble()
}
