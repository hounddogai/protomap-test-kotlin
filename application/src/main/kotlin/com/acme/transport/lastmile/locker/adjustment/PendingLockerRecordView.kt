package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending locker record. */
data class PendingLockerRecordView(
    var code: String,
    var label: String,
    var name: String,
    var score: String,
    var weightText: String,
    var unit: String,
    var approved: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toPendingLockerRecordView(item: PendingLockerRecord): PendingLockerRecordView {
    return PendingLockerRecordView(
        code = item.code,
        label = item.label,
        name = truncate(item.name, 120),
        score = "%.2f".format(item.score),
        weightText = "%.2f".format(item.weight),
        unit = item.unit,
        approved = "${item.approved}",
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingLockerRecordViews(items: MutableList<PendingLockerRecord>): MutableList<PendingLockerRecordView> {
    val views = mutableListOf<PendingLockerRecordView>()
    for (item in items) {
        views.add(toPendingLockerRecordView(item))
    }
    return views
}

fun sumPendingLockerRecordView(view: PendingLockerRecordView): Double {
    val parsedScore = view.score.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedScore + parsedWeight
}
