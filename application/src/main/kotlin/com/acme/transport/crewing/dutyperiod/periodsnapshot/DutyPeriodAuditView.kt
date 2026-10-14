package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period audit. */
data class DutyPeriodAuditView(
    var codeText: String,
    var name: String,
    var quantityText: String,
    var contactEmailText: String,
    var budget: String,
    var summary: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toDutyPeriodAuditView(item: DutyPeriodAudit): DutyPeriodAuditView {
    return DutyPeriodAuditView(
        codeText = item.code,
        name = item.name,
        quantityText = item.quantity.toString(),
        contactEmailText = item.contactEmail,
        budget = "%.2f".format(item.budget),
        summary = truncate(item.summary, 120),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodAuditViews(items: MutableList<DutyPeriodAudit>): MutableList<DutyPeriodAuditView> {
    val views = mutableListOf<DutyPeriodAuditView>()
    for (item in items) {
        views.add(toDutyPeriodAuditView(item))
    }
    return views
}

fun sumDutyPeriodAuditView(view: DutyPeriodAuditView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedQuantity.toDouble() + parsedBudget + parsedDurationMinutes.toDouble()
}
