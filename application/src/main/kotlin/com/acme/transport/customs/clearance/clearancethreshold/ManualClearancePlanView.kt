package com.acme.transport.customs.clearance.clearancethreshold

/** How screens and exports show a manual clearance plan. */
data class ManualClearancePlanView(
    var code: String,
    var name: String,
    var approvedText: String,
    var durationMinutesText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toManualClearancePlanView(item: ManualClearancePlan): ManualClearancePlanView {
    return ManualClearancePlanView(
        code = item.code,
        name = item.name,
        approvedText = "${item.approved}",
        durationMinutesText = item.durationMinutes.toString(),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualClearancePlanViews(items: MutableList<ManualClearancePlan>): MutableList<ManualClearancePlanView> {
    val views = mutableListOf<ManualClearancePlanView>()
    for (item in items) {
        views.add(toManualClearancePlanView(item))
    }
    return views
}

fun sumManualClearancePlanView(view: ManualClearancePlanView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedDurationMinutes.toDouble() + parsedStopCount.toDouble()
}
