package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell ledger. */
data class DwellLedgerView(
    var code: String,
    var name: String,
    var countryText: String,
    var revision: String,
    var versionText: String,
    var statusLabel: String,
)

fun toDwellLedgerView(item: DwellLedger): DwellLedgerView {
    return DwellLedgerView(
        code = item.code,
        name = item.name,
        countryText = truncate(item.country, 64),
        revision = item.revision.toString(),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellLedgerViews(items: MutableList<DwellLedger>): MutableList<DwellLedgerView> {
    val views = mutableListOf<DwellLedgerView>()
    for (item in items) {
        views.add(toDwellLedgerView(item))
    }
    return views
}

fun sumDwellLedgerView(view: DwellLedgerView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedRevision.toDouble() + parsedVersion.toDouble()
}
