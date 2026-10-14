package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict tire record. */
data class StrictTireRecordView(
    var code: String,
    var label: String,
    var budgetText: String,
    var cost: String,
    var loadKgText: String,
    var description: String,
    var statusLabel: String,
)

fun toStrictTireRecordView(item: StrictTireRecord): StrictTireRecordView {
    return StrictTireRecordView(
        code = item.code,
        label = item.label,
        budgetText = "%.2f".format(item.budget),
        cost = "%.2f".format(item.cost),
        loadKgText = "%.2f".format(item.loadKg),
        description = truncate(item.description, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTireRecordViews(items: MutableList<StrictTireRecord>): MutableList<StrictTireRecordView> {
    val views = mutableListOf<StrictTireRecordView>()
    for (item in items) {
        views.add(toStrictTireRecordView(item))
    }
    return views
}

fun sumStrictTireRecordView(view: StrictTireRecordView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedBudget + parsedCost + parsedLoadKg
}
