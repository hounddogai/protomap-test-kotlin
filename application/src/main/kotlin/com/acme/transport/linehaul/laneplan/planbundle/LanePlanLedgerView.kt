package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan ledger. */
data class LanePlanLedgerView(
    var code: String,
    var nameText: String,
    var graceDaysText: String,
    var pinnedText: String,
    var sequence: String,
    var source: String,
    var labelText: String,
    var statusLabel: String,
)

fun toLanePlanLedgerView(item: LanePlanLedger): LanePlanLedgerView {
    return LanePlanLedgerView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 120),
        graceDaysText = item.graceDays.toString(),
        pinnedText = "${item.pinned}",
        sequence = item.sequence.toString(),
        source = item.source,
        labelText = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanLedgerViews(items: MutableList<LanePlanLedger>): MutableList<LanePlanLedgerView> {
    val views = mutableListOf<LanePlanLedgerView>()
    for (item in items) {
        views.add(toLanePlanLedgerView(item))
    }
    return views
}

fun sumLanePlanLedgerView(view: LanePlanLedgerView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedGraceDays.toDouble() + parsedSequence.toDouble()
}
