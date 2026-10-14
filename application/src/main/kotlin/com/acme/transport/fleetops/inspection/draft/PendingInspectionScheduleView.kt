package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending inspection schedule. */
data class PendingInspectionScheduleView(
    var code: String,
    var title: String,
    var loadKgText: String,
    var scoreText: String,
    var depot: String,
    var rate: String,
    var odometer: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toPendingInspectionScheduleView(item: PendingInspectionSchedule): PendingInspectionScheduleView {
    return PendingInspectionScheduleView(
        code = item.code,
        title = truncate(item.title, 64),
        loadKgText = "%.2f".format(item.loadKg),
        scoreText = "%.2f".format(item.score),
        depot = item.depot,
        rate = "%.2f".format(item.rate),
        odometer = item.odometer.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingInspectionScheduleViews(items: MutableList<PendingInspectionSchedule>): MutableList<PendingInspectionScheduleView> {
    val views = mutableListOf<PendingInspectionScheduleView>()
    for (item in items) {
        views.add(toPendingInspectionScheduleView(item))
    }
    return views
}

fun sumPendingInspectionScheduleView(view: PendingInspectionScheduleView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedLoadKg + parsedScore + parsedRate
}
