package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance budget. */
data class ClearanceBudgetView(
    var codeText: String,
    var name: String,
    var floorValueText: String,
    var depot: String,
    var source: String,
    var thresholdText: String,
    var title: String,
    var locale: String,
    var sequenceText: String,
    var shiftCode: String,
    var budget: String,
    var statusLabel: String,
)

fun toClearanceBudgetView(item: ClearanceBudget): ClearanceBudgetView {
    return ClearanceBudgetView(
        codeText = item.code,
        name = item.name,
        floorValueText = "%.2f".format(item.floorValue),
        depot = item.depot,
        source = truncate(item.source, 40),
        thresholdText = "%.2f".format(item.threshold),
        title = truncate(item.title, 80),
        locale = item.locale,
        sequenceText = item.sequence.toString(),
        shiftCode = item.shiftCode,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceBudgetViews(items: MutableList<ClearanceBudget>): MutableList<ClearanceBudgetView> {
    val views = mutableListOf<ClearanceBudgetView>()
    for (item in items) {
        views.add(toClearanceBudgetView(item))
    }
    return views
}

fun sumClearanceBudgetView(view: ClearanceBudgetView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedFloorValue + parsedThreshold + parsedSequence.toDouble()
}
