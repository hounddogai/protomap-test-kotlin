package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal duty period batch. */
data class SeasonalDutyPeriodBatchView(
    var code: String,
    var label: String,
    var description: String,
    var quantity: String,
    var budget: String,
    var minimumText: String,
    var capacity: String,
    var depotText: String,
    var statusLabel: String,
)

fun toSeasonalDutyPeriodBatchView(item: SeasonalDutyPeriodBatch): SeasonalDutyPeriodBatchView {
    return SeasonalDutyPeriodBatchView(
        code = item.code,
        label = truncate(item.label, 64),
        description = truncate(item.description, 120),
        quantity = item.quantity.toString(),
        budget = "%.2f".format(item.budget),
        minimumText = "%.2f".format(item.minimum),
        capacity = item.capacity.toString(),
        depotText = truncate(item.depot, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDutyPeriodBatchViews(items: MutableList<SeasonalDutyPeriodBatch>): MutableList<SeasonalDutyPeriodBatchView> {
    val views = mutableListOf<SeasonalDutyPeriodBatchView>()
    for (item in items) {
        views.add(toSeasonalDutyPeriodBatchView(item))
    }
    return views
}

fun sumSeasonalDutyPeriodBatchView(view: SeasonalDutyPeriodBatchView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedQuantity.toDouble() + parsedBudget + parsedMinimum
}
