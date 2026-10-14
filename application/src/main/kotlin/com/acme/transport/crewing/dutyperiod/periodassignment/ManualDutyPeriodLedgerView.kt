package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual duty period ledger. */
data class ManualDutyPeriodLedgerView(
    var code: String,
    var label: String,
    var archivedText: String,
    var capacity: String,
    var notes: String,
    var lane: String,
    var limit: String,
    var maximumText: String,
    var startsAt: String,
    var checksum: String,
    var phone: String,
    var statusLabel: String,
)

fun toManualDutyPeriodLedgerView(item: ManualDutyPeriodLedger): ManualDutyPeriodLedgerView {
    return ManualDutyPeriodLedgerView(
        code = truncate(item.code, 40),
        label = item.label,
        archivedText = "${item.archived}",
        capacity = item.capacity.toString(),
        notes = item.notes,
        lane = item.lane,
        limit = item.limit.toString(),
        maximumText = "%.2f".format(item.maximum),
        startsAt = item.startsAt.toString(),
        checksum = item.checksum,
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDutyPeriodLedgerViews(items: MutableList<ManualDutyPeriodLedger>): MutableList<ManualDutyPeriodLedgerView> {
    val views = mutableListOf<ManualDutyPeriodLedgerView>()
    for (item in items) {
        views.add(toManualDutyPeriodLedgerView(item))
    }
    return views
}

fun sumManualDutyPeriodLedgerView(view: ManualDutyPeriodLedgerView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedCapacity.toDouble() + parsedLimit.toDouble() + parsedMaximum
}
