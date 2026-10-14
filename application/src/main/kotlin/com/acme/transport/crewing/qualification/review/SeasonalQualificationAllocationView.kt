package com.acme.transport.crewing.qualification.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal qualification allocation. */
data class SeasonalQualificationAllocationView(
    var code: String,
    var name: String,
    var graceDays: String,
    var stopCount: String,
    var ceilingText: String,
    var priorityText: String,
    var depotText: String,
    var routeCode: String,
    var trailerId: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toSeasonalQualificationAllocationView(item: SeasonalQualificationAllocation): SeasonalQualificationAllocationView {
    return SeasonalQualificationAllocationView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 40),
        graceDays = item.graceDays.toString(),
        stopCount = item.stopCount.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        priorityText = item.priority.toString(),
        depotText = item.depot,
        routeCode = item.routeCode,
        trailerId = truncate(item.trailerId, 40),
        shiftCode = truncate(item.shiftCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalQualificationAllocationViews(items: MutableList<SeasonalQualificationAllocation>): MutableList<SeasonalQualificationAllocationView> {
    val views = mutableListOf<SeasonalQualificationAllocationView>()
    for (item in items) {
        views.add(toSeasonalQualificationAllocationView(item))
    }
    return views
}

fun sumSeasonalQualificationAllocationView(view: SeasonalQualificationAllocationView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedGraceDays.toDouble() + parsedStopCount.toDouble() + parsedCeiling
}
