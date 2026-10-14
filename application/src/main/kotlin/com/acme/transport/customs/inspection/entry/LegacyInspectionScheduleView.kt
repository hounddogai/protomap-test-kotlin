package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy inspection schedule. */
data class LegacyInspectionScheduleView(
    var code: String,
    var label: String,
    var versionText: String,
    var endsAt: String,
    var limit: String,
    var durationMinutes: String,
    var segment: String,
    var etaMinutesText: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toLegacyInspectionScheduleView(item: LegacyInspectionSchedule): LegacyInspectionScheduleView {
    return LegacyInspectionScheduleView(
        code = truncate(item.code, 40),
        label = item.label,
        versionText = item.version.toString(),
        endsAt = item.endsAt.toString(),
        limit = item.limit.toString(),
        durationMinutes = item.durationMinutes.toString(),
        segment = item.segment,
        etaMinutesText = item.etaMinutes.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionScheduleViews(items: MutableList<LegacyInspectionSchedule>): MutableList<LegacyInspectionScheduleView> {
    val views = mutableListOf<LegacyInspectionScheduleView>()
    for (item in items) {
        views.add(toLegacyInspectionScheduleView(item))
    }
    return views
}

fun sumLegacyInspectionScheduleView(view: LegacyInspectionScheduleView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedVersion.toDouble() + parsedEndsAt.toDouble() + parsedLimit.toDouble()
}
