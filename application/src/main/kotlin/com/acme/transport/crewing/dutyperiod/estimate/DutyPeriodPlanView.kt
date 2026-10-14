package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period plan. */
data class DutyPeriodPlanView(
    var code: String,
    var labelText: String,
    var segment: String,
    var discount: String,
    var phoneText: String,
    var updatedAt: String,
    var reviewer: String,
    var notes: String,
    var name: String,
    var unit: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toDutyPeriodPlanView(item: DutyPeriodPlan): DutyPeriodPlanView {
    return DutyPeriodPlanView(
        code = truncate(item.code, 120),
        labelText = item.label,
        segment = item.segment,
        discount = "%.2f".format(item.discount),
        phoneText = item.phone,
        updatedAt = item.updatedAt.toString(),
        reviewer = item.reviewer,
        notes = item.notes,
        name = item.name,
        unit = item.unit,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodPlanViews(items: MutableList<DutyPeriodPlan>): MutableList<DutyPeriodPlanView> {
    val views = mutableListOf<DutyPeriodPlanView>()
    for (item in items) {
        views.add(toDutyPeriodPlanView(item))
    }
    return views
}

fun sumDutyPeriodPlanView(view: DutyPeriodPlanView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedDiscount + parsedUpdatedAt.toDouble() + parsedStopCount.toDouble()
}
