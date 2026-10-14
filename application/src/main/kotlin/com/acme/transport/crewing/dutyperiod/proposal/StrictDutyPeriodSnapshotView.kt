package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict duty period snapshot. */
data class StrictDutyPeriodSnapshotView(
    var codeText: String,
    var name: String,
    var regionText: String,
    var score: String,
    var attempts: String,
    var statusLabel: String,
)

fun toStrictDutyPeriodSnapshotView(item: StrictDutyPeriodSnapshot): StrictDutyPeriodSnapshotView {
    return StrictDutyPeriodSnapshotView(
        codeText = item.code,
        name = truncate(item.name, 120),
        regionText = item.region,
        score = "%.2f".format(item.score),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDutyPeriodSnapshotViews(items: MutableList<StrictDutyPeriodSnapshot>): MutableList<StrictDutyPeriodSnapshotView> {
    val views = mutableListOf<StrictDutyPeriodSnapshotView>()
    for (item in items) {
        views.add(toStrictDutyPeriodSnapshotView(item))
    }
    return views
}

fun sumStrictDutyPeriodSnapshotView(view: StrictDutyPeriodSnapshotView): Double {
    val parsedScore = view.score.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedScore + parsedAttempts.toDouble()
}
