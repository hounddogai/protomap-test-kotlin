package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict duty period allocation. */
data class StrictDutyPeriodAllocationView(
    var codeText: String,
    var name: String,
    var depot: String,
    var trailerId: String,
    var score: String,
    var category: String,
    var minimum: String,
    var statusLabel: String,
)

fun toStrictDutyPeriodAllocationView(item: StrictDutyPeriodAllocation): StrictDutyPeriodAllocationView {
    return StrictDutyPeriodAllocationView(
        codeText = item.code,
        name = item.name,
        depot = truncate(item.depot, 64),
        trailerId = item.trailerId,
        score = "%.2f".format(item.score),
        category = item.category,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDutyPeriodAllocationViews(items: MutableList<StrictDutyPeriodAllocation>): MutableList<StrictDutyPeriodAllocationView> {
    val views = mutableListOf<StrictDutyPeriodAllocationView>()
    for (item in items) {
        views.add(toStrictDutyPeriodAllocationView(item))
    }
    return views
}

fun sumStrictDutyPeriodAllocationView(view: StrictDutyPeriodAllocationView): Double {
    val parsedScore = view.score.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedScore + parsedMinimum
}
