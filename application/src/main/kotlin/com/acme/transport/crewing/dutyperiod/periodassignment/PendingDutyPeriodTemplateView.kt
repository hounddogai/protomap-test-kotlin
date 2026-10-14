package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending duty period template. */
data class PendingDutyPeriodTemplateView(
    var code: String,
    var label: String,
    var externalRef: String,
    var contactEmail: String,
    var capacityText: String,
    var reviewerText: String,
    var depot: String,
    var windowDays: String,
    var stopCount: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toPendingDutyPeriodTemplateView(item: PendingDutyPeriodTemplate): PendingDutyPeriodTemplateView {
    return PendingDutyPeriodTemplateView(
        code = item.code,
        label = item.label,
        externalRef = item.externalRef,
        contactEmail = item.contactEmail,
        capacityText = item.capacity.toString(),
        reviewerText = item.reviewer,
        depot = item.depot,
        windowDays = item.windowDays.toString(),
        stopCount = item.stopCount.toString(),
        categoryText = truncate(item.category, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDutyPeriodTemplateViews(items: MutableList<PendingDutyPeriodTemplate>): MutableList<PendingDutyPeriodTemplateView> {
    val views = mutableListOf<PendingDutyPeriodTemplateView>()
    for (item in items) {
        views.add(toPendingDutyPeriodTemplateView(item))
    }
    return views
}

fun sumPendingDutyPeriodTemplateView(view: PendingDutyPeriodTemplateView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedCapacity.toDouble() + parsedWindowDays.toDouble() + parsedStopCount.toDouble()
}
