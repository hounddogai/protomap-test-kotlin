package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary roster record. */
data class PrimaryRosterRecordView(
    var code: String,
    var name: String,
    var currency: String,
    var depot: String,
    var lane: String,
    var batchSize: String,
    var timezoneText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toPrimaryRosterRecordView(item: PrimaryRosterRecord): PrimaryRosterRecordView {
    return PrimaryRosterRecordView(
        code = item.code,
        name = item.name,
        currency = item.currency,
        depot = item.depot,
        lane = truncate(item.lane, 80),
        batchSize = item.batchSize.toString(),
        timezoneText = item.timezone,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRosterRecordViews(items: MutableList<PrimaryRosterRecord>): MutableList<PrimaryRosterRecordView> {
    val views = mutableListOf<PrimaryRosterRecordView>()
    for (item in items) {
        views.add(toPrimaryRosterRecordView(item))
    }
    return views
}

fun sumPrimaryRosterRecordView(view: PrimaryRosterRecordView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedBatchSize.toDouble() + parsedLoadKg
}
