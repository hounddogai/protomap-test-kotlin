package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect budget. */
data class DefectBudgetView(
    var code: String,
    var labelText: String,
    var archived: String,
    var locale: String,
    var floorValue: String,
    var amountText: String,
    var summary: String,
    var statusLabel: String,
)

fun toDefectBudgetView(item: DefectBudget): DefectBudgetView {
    return DefectBudgetView(
        code = truncate(item.code, 80),
        labelText = truncate(item.label, 40),
        archived = "${item.archived}",
        locale = truncate(item.locale, 120),
        floorValue = "%.2f".format(item.floorValue),
        amountText = "%.2f".format(item.amount),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectBudgetViews(items: MutableList<DefectBudget>): MutableList<DefectBudgetView> {
    val views = mutableListOf<DefectBudgetView>()
    for (item in items) {
        views.add(toDefectBudgetView(item))
    }
    return views
}

fun sumDefectBudgetView(view: DefectBudgetView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedFloorValue + parsedAmount
}
