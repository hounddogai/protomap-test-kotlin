package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped dwell draft. */
data class CappedDwellDraftView(
    var code: String,
    var name: String,
    var limit: String,
    var approvedText: String,
    var checksum: String,
    var stopCount: String,
    var locale: String,
    var statusLabel: String,
)

fun toCappedDwellDraftView(item: CappedDwellDraft): CappedDwellDraftView {
    return CappedDwellDraftView(
        code = item.code,
        name = truncate(item.name, 64),
        limit = item.limit.toString(),
        approvedText = "${item.approved}",
        checksum = item.checksum,
        stopCount = item.stopCount.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDwellDraftViews(items: MutableList<CappedDwellDraft>): MutableList<CappedDwellDraftView> {
    val views = mutableListOf<CappedDwellDraftView>()
    for (item in items) {
        views.add(toCappedDwellDraftView(item))
    }
    return views
}

fun sumCappedDwellDraftView(view: CappedDwellDraftView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedLimit.toDouble() + parsedStopCount.toDouble()
}
