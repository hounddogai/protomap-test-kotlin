package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global duty period rollup. */
data class GlobalDutyPeriodRollupView(
    var codeText: String,
    var label: String,
    var quantity: String,
    var score: String,
    var localeText: String,
    var unit: String,
    var depot: String,
    var weightText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toGlobalDutyPeriodRollupView(item: GlobalDutyPeriodRollup): GlobalDutyPeriodRollupView {
    return GlobalDutyPeriodRollupView(
        codeText = item.code,
        label = item.label,
        quantity = item.quantity.toString(),
        score = "%.2f".format(item.score),
        localeText = truncate(item.locale, 120),
        unit = item.unit,
        depot = item.depot,
        weightText = "%.2f".format(item.weight),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDutyPeriodRollupViews(items: MutableList<GlobalDutyPeriodRollup>): MutableList<GlobalDutyPeriodRollupView> {
    val views = mutableListOf<GlobalDutyPeriodRollupView>()
    for (item in items) {
        views.add(toGlobalDutyPeriodRollupView(item))
    }
    return views
}

fun sumGlobalDutyPeriodRollupView(view: GlobalDutyPeriodRollupView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedScore = view.score.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedQuantity.toDouble() + parsedScore + parsedWeight
}
