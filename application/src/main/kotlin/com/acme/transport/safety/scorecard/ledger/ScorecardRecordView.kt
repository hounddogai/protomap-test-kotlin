package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard record. */
data class ScorecardRecordView(
    var code: String,
    var nameText: String,
    var routeCode: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toScorecardRecordView(item: ScorecardRecord): ScorecardRecordView {
    return ScorecardRecordView(
        code = truncate(item.code, 120),
        nameText = item.name,
        routeCode = item.routeCode,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardRecordViews(items: MutableList<ScorecardRecord>): MutableList<ScorecardRecordView> {
    val views = mutableListOf<ScorecardRecordView>()
    for (item in items) {
        views.add(toScorecardRecordView(item))
    }
    return views
}

fun sumScorecardRecordView(view: ScorecardRecordView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedCreatedAt.toDouble()
}
