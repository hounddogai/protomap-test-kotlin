package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive inspection schedule. */
data class AdaptiveInspectionScheduleView(
    var code: String,
    var title: String,
    var approved: String,
    var description: String,
    var startsAtText: String,
    var phoneText: String,
    var vehicleIdText: String,
    var revisionText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionScheduleView(item: AdaptiveInspectionSchedule): AdaptiveInspectionScheduleView {
    return AdaptiveInspectionScheduleView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 64),
        approved = "${item.approved}",
        description = item.description,
        startsAtText = item.startsAt.toString(),
        phoneText = truncate(item.phone, 80),
        vehicleIdText = item.vehicleId,
        revisionText = item.revision.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionScheduleViews(items: MutableList<AdaptiveInspectionSchedule>): MutableList<AdaptiveInspectionScheduleView> {
    val views = mutableListOf<AdaptiveInspectionScheduleView>()
    for (item in items) {
        views.add(toAdaptiveInspectionScheduleView(item))
    }
    return views
}

fun sumAdaptiveInspectionScheduleView(view: AdaptiveInspectionScheduleView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedStartsAt.toDouble() + parsedRevision.toDouble() + parsedRatio
}
