package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling attempt record. */
data class RollingAttemptRecordView(
    var code: String,
    var labelText: String,
    var depot: String,
    var threshold: String,
    var notes: String,
    var driverId: String,
    var region: String,
    var shiftCodeText: String,
    var active: String,
    var phoneText: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toRollingAttemptRecordView(item: RollingAttemptRecord): RollingAttemptRecordView {
    return RollingAttemptRecordView(
        code = item.code,
        labelText = item.label,
        depot = truncate(item.depot, 120),
        threshold = "%.2f".format(item.threshold),
        notes = truncate(item.notes, 80),
        driverId = item.driverId,
        region = truncate(item.region, 120),
        shiftCodeText = truncate(item.shiftCode, 64),
        active = "${item.active}",
        phoneText = truncate(item.phone, 40),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAttemptRecordViews(items: MutableList<RollingAttemptRecord>): MutableList<RollingAttemptRecordView> {
    val views = mutableListOf<RollingAttemptRecordView>()
    for (item in items) {
        views.add(toRollingAttemptRecordView(item))
    }
    return views
}

fun sumRollingAttemptRecordView(view: RollingAttemptRecordView): Double {
    val parsedThreshold = view.threshold.toDouble()
    return parsedThreshold
}
