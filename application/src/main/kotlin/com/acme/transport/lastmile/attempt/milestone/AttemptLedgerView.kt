package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt ledger. */
data class AttemptLedgerView(
    var codeText: String,
    var nameText: String,
    var channel: String,
    var graceDays: String,
    var revision: String,
    var visible: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toAttemptLedgerView(item: AttemptLedger): AttemptLedgerView {
    return AttemptLedgerView(
        codeText = item.code,
        nameText = truncate(item.name, 120),
        channel = item.channel,
        graceDays = item.graceDays.toString(),
        revision = item.revision.toString(),
        visible = "${item.visible}",
        segmentText = truncate(item.segment, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptLedgerViews(items: MutableList<AttemptLedger>): MutableList<AttemptLedgerView> {
    val views = mutableListOf<AttemptLedgerView>()
    for (item in items) {
        views.add(toAttemptLedgerView(item))
    }
    return views
}

fun sumAttemptLedgerView(view: AttemptLedgerView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedGraceDays.toDouble() + parsedRevision.toDouble()
}
