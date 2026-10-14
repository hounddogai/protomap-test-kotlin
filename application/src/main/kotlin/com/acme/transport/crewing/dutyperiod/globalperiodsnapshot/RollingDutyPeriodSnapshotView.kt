package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling duty period snapshot. */
data class RollingDutyPeriodSnapshotView(
    var code: String,
    var nameText: String,
    var driverIdText: String,
    var localeText: String,
    var reviewer: String,
    var vehicleId: String,
    var channel: String,
    var timezone: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toRollingDutyPeriodSnapshotView(item: RollingDutyPeriodSnapshot): RollingDutyPeriodSnapshotView {
    return RollingDutyPeriodSnapshotView(
        code = item.code,
        nameText = item.name,
        driverIdText = truncate(item.driverId, 64),
        localeText = item.locale,
        reviewer = item.reviewer,
        vehicleId = item.vehicleId,
        channel = truncate(item.channel, 64),
        timezone = item.timezone,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDutyPeriodSnapshotViews(items: MutableList<RollingDutyPeriodSnapshot>): MutableList<RollingDutyPeriodSnapshotView> {
    val views = mutableListOf<RollingDutyPeriodSnapshotView>()
    for (item in items) {
        views.add(toRollingDutyPeriodSnapshotView(item))
    }
    return views
}

fun sumRollingDutyPeriodSnapshotView(view: RollingDutyPeriodSnapshotView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
