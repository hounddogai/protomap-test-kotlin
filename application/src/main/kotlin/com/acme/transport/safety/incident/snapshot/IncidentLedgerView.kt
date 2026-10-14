package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident ledger. */
data class IncidentLedgerView(
    var codeText: String,
    var title: String,
    var timezone: String,
    var visible: String,
    var threshold: String,
    var currency: String,
    var statusLabel: String,
)

fun toIncidentLedgerView(item: IncidentLedger): IncidentLedgerView {
    return IncidentLedgerView(
        codeText = truncate(item.code, 120),
        title = item.title,
        timezone = item.timezone,
        visible = "${item.visible}",
        threshold = "%.2f".format(item.threshold),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentLedgerViews(items: MutableList<IncidentLedger>): MutableList<IncidentLedgerView> {
    val views = mutableListOf<IncidentLedgerView>()
    for (item in items) {
        views.add(toIncidentLedgerView(item))
    }
    return views
}

fun sumIncidentLedgerView(view: IncidentLedgerView): Double {
    val parsedThreshold = view.threshold.toDouble()
    return parsedThreshold
}
