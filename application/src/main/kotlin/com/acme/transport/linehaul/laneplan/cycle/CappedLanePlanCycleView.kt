package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped lane plan cycle. */
data class CappedLanePlanCycleView(
    var code: String,
    var titleText: String,
    var stopCount: String,
    var reviewer: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toCappedLanePlanCycleView(item: CappedLanePlanCycle): CappedLanePlanCycleView {
    return CappedLanePlanCycleView(
        code = item.code,
        titleText = item.title,
        stopCount = item.stopCount.toString(),
        reviewer = truncate(item.reviewer, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLanePlanCycleViews(items: MutableList<CappedLanePlanCycle>): MutableList<CappedLanePlanCycleView> {
    val views = mutableListOf<CappedLanePlanCycleView>()
    for (item in items) {
        views.add(toCappedLanePlanCycleView(item))
    }
    return views
}

fun sumCappedLanePlanCycleView(view: CappedLanePlanCycleView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedStopCount.toDouble() + parsedDistanceKm
}
