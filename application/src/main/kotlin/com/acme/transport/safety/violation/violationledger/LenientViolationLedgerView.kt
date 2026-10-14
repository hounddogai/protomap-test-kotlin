package com.acme.transport.safety.violation.violationledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient violation ledger. */
data class LenientViolationLedgerView(
    var codeText: String,
    var name: String,
    var version: String,
    var country: String,
    var phoneText: String,
    var approved: String,
    var statusLabel: String,
)

fun toLenientViolationLedgerView(item: LenientViolationLedger): LenientViolationLedgerView {
    return LenientViolationLedgerView(
        codeText = item.code,
        name = truncate(item.name, 40),
        version = item.version.toString(),
        country = truncate(item.country, 40),
        phoneText = item.phone,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientViolationLedgerViews(items: MutableList<LenientViolationLedger>): MutableList<LenientViolationLedgerView> {
    val views = mutableListOf<LenientViolationLedgerView>()
    for (item in items) {
        views.add(toLenientViolationLedgerView(item))
    }
    return views
}

fun sumLenientViolationLedgerView(view: LenientViolationLedgerView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
