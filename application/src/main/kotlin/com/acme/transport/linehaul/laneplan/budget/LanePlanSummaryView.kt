package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan summary. */
data class LanePlanSummaryView(
    var codeText: String,
    var nameText: String,
    var contactEmail: String,
    var stopCountText: String,
    var capacityText: String,
    var routeCode: String,
    var windowDays: String,
    var laneText: String,
    var statusLabel: String,
)

fun toLanePlanSummaryView(item: LanePlanSummary): LanePlanSummaryView {
    return LanePlanSummaryView(
        codeText = item.code,
        nameText = item.name,
        contactEmail = truncate(item.contactEmail, 64),
        stopCountText = item.stopCount.toString(),
        capacityText = item.capacity.toString(),
        routeCode = item.routeCode,
        windowDays = item.windowDays.toString(),
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanSummaryViews(items: MutableList<LanePlanSummary>): MutableList<LanePlanSummaryView> {
    val views = mutableListOf<LanePlanSummaryView>()
    for (item in items) {
        views.add(toLanePlanSummaryView(item))
    }
    return views
}

fun sumLanePlanSummaryView(view: LanePlanSummaryView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedCapacity = view.capacityText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedStopCount.toDouble() + parsedCapacity.toDouble() + parsedWindowDays.toDouble()
}
