package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted duty period claim. */
data class WeightedDutyPeriodClaimView(
    var codeText: String,
    var title: String,
    var visible: String,
    var quantity: String,
    var regionText: String,
    var durationMinutesText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toWeightedDutyPeriodClaimView(item: WeightedDutyPeriodClaim): WeightedDutyPeriodClaimView {
    return WeightedDutyPeriodClaimView(
        codeText = item.code,
        title = truncate(item.title, 80),
        visible = "${item.visible}",
        quantity = item.quantity.toString(),
        regionText = truncate(item.region, 120),
        durationMinutesText = item.durationMinutes.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDutyPeriodClaimViews(items: MutableList<WeightedDutyPeriodClaim>): MutableList<WeightedDutyPeriodClaimView> {
    val views = mutableListOf<WeightedDutyPeriodClaimView>()
    for (item in items) {
        views.add(toWeightedDutyPeriodClaimView(item))
    }
    return views
}

fun sumWeightedDutyPeriodClaimView(view: WeightedDutyPeriodClaimView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedQuantity.toDouble() + parsedDurationMinutes.toDouble()
}
