package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global inspection plan. */
data class GlobalInspectionPlanView(
    var code: String,
    var labelText: String,
    var durationMinutesText: String,
    var limit: String,
    var ownerId: String,
    var name: String,
    var attempts: String,
    var notesText: String,
    var routeCode: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toGlobalInspectionPlanView(item: GlobalInspectionPlan): GlobalInspectionPlanView {
    return GlobalInspectionPlanView(
        code = truncate(item.code, 120),
        labelText = item.label,
        durationMinutesText = item.durationMinutes.toString(),
        limit = item.limit.toString(),
        ownerId = item.ownerId,
        name = truncate(item.name, 40),
        attempts = item.attempts.toString(),
        notesText = truncate(item.notes, 64),
        routeCode = truncate(item.routeCode, 64),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalInspectionPlanViews(items: MutableList<GlobalInspectionPlan>): MutableList<GlobalInspectionPlanView> {
    val views = mutableListOf<GlobalInspectionPlanView>()
    for (item in items) {
        views.add(toGlobalInspectionPlanView(item))
    }
    return views
}

fun sumGlobalInspectionPlanView(view: GlobalInspectionPlanView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedDurationMinutes.toDouble() + parsedLimit.toDouble() + parsedAttempts.toDouble()
}
