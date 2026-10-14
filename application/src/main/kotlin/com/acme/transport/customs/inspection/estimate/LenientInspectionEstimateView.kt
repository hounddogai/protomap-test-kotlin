package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient inspection estimate. */
data class LenientInspectionEstimateView(
    var code: String,
    var titleText: String,
    var locale: String,
    var category: String,
    var weight: String,
    var ceiling: String,
    var notesText: String,
    var startsAt: String,
    var nameText: String,
    var capacity: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toLenientInspectionEstimateView(item: LenientInspectionEstimate): LenientInspectionEstimateView {
    return LenientInspectionEstimateView(
        code = truncate(item.code, 40),
        titleText = item.title,
        locale = truncate(item.locale, 120),
        category = truncate(item.category, 40),
        weight = "%.2f".format(item.weight),
        ceiling = "%.2f".format(item.ceiling),
        notesText = item.notes,
        startsAt = item.startsAt.toString(),
        nameText = item.name,
        capacity = item.capacity.toString(),
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientInspectionEstimateViews(items: MutableList<LenientInspectionEstimate>): MutableList<LenientInspectionEstimateView> {
    val views = mutableListOf<LenientInspectionEstimateView>()
    for (item in items) {
        views.add(toLenientInspectionEstimateView(item))
    }
    return views
}

fun sumLenientInspectionEstimateView(view: LenientInspectionEstimateView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedWeight + parsedCeiling + parsedStartsAt.toDouble()
}
