package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted duty period target. */
data class WeightedDutyPeriodTargetView(
    var codeText: String,
    var name: String,
    var phone: String,
    var contactEmail: String,
    var loadKg: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toWeightedDutyPeriodTargetView(item: WeightedDutyPeriodTarget): WeightedDutyPeriodTargetView {
    return WeightedDutyPeriodTargetView(
        codeText = truncate(item.code, 120),
        name = truncate(item.name, 80),
        phone = item.phone,
        contactEmail = truncate(item.contactEmail, 80),
        loadKg = "%.2f".format(item.loadKg),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDutyPeriodTargetViews(items: MutableList<WeightedDutyPeriodTarget>): MutableList<WeightedDutyPeriodTargetView> {
    val views = mutableListOf<WeightedDutyPeriodTargetView>()
    for (item in items) {
        views.add(toWeightedDutyPeriodTargetView(item))
    }
    return views
}

fun sumWeightedDutyPeriodTargetView(view: WeightedDutyPeriodTargetView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedLoadKg + parsedFloorValue
}
