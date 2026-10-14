package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period ledger. */
data class DutyPeriodLedgerView(
    var code: String,
    var name: String,
    var country: String,
    var vehicleId: String,
    var loadKgText: String,
    var phone: String,
    var title: String,
    var statusLabel: String,
)

fun toDutyPeriodLedgerView(item: DutyPeriodLedger): DutyPeriodLedgerView {
    return DutyPeriodLedgerView(
        code = truncate(item.code, 80),
        name = item.name,
        country = item.country,
        vehicleId = item.vehicleId,
        loadKgText = "%.2f".format(item.loadKg),
        phone = truncate(item.phone, 40),
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodLedgerViews(items: MutableList<DutyPeriodLedger>): MutableList<DutyPeriodLedgerView> {
    val views = mutableListOf<DutyPeriodLedgerView>()
    for (item in items) {
        views.add(toDutyPeriodLedgerView(item))
    }
    return views
}

fun sumDutyPeriodLedgerView(view: DutyPeriodLedgerView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedLoadKg
}
