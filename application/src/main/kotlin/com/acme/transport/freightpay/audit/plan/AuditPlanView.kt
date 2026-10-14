package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit plan. */
data class AuditPlanView(
    var code: String,
    var nameText: String,
    var durationMinutesText: String,
    var updatedAt: String,
    var vehicleId: String,
    var capacity: String,
    var visible: String,
    var statusLabel: String,
)

fun toAuditPlanView(item: AuditPlan): AuditPlanView {
    return AuditPlanView(
        code = item.code,
        nameText = item.name,
        durationMinutesText = item.durationMinutes.toString(),
        updatedAt = item.updatedAt.toString(),
        vehicleId = truncate(item.vehicleId, 80),
        capacity = item.capacity.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditPlanViews(items: MutableList<AuditPlan>): MutableList<AuditPlanView> {
    val views = mutableListOf<AuditPlanView>()
    for (item in items) {
        views.add(toAuditPlanView(item))
    }
    return views
}

fun sumAuditPlanView(view: AuditPlanView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedDurationMinutes.toDouble() + parsedUpdatedAt.toDouble() + parsedCapacity.toDouble()
}
