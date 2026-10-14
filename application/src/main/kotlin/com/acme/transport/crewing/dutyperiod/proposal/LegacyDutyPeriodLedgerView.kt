package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy duty period ledger. */
data class LegacyDutyPeriodLedgerView(
    var code: String,
    var nameText: String,
    var quantityText: String,
    var externalRef: String,
    var endsAt: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toLegacyDutyPeriodLedgerView(item: LegacyDutyPeriodLedger): LegacyDutyPeriodLedgerView {
    return LegacyDutyPeriodLedgerView(
        code = item.code,
        nameText = item.name,
        quantityText = item.quantity.toString(),
        externalRef = item.externalRef,
        endsAt = item.endsAt.toString(),
        summaryText = truncate(item.summary, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDutyPeriodLedgerViews(items: MutableList<LegacyDutyPeriodLedger>): MutableList<LegacyDutyPeriodLedgerView> {
    val views = mutableListOf<LegacyDutyPeriodLedgerView>()
    for (item in items) {
        views.add(toLegacyDutyPeriodLedgerView(item))
    }
    return views
}

fun sumLegacyDutyPeriodLedgerView(view: LegacyDutyPeriodLedgerView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedQuantity.toDouble() + parsedEndsAt.toDouble()
}
