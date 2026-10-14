package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation plan. */
data class ViolationPlanView(
    var codeText: String,
    var labelText: String,
    var timezoneText: String,
    var externalRefText: String,
    var updatedAt: String,
    var visibleText: String,
    var distanceKm: String,
    var retries: String,
    var source: String,
    var approvedText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toViolationPlanView(item: ViolationPlan): ViolationPlanView {
    return ViolationPlanView(
        codeText = item.code,
        labelText = item.label,
        timezoneText = item.timezone,
        externalRefText = truncate(item.externalRef, 64),
        updatedAt = item.updatedAt.toString(),
        visibleText = "${item.visible}",
        distanceKm = "%.2f".format(item.distanceKm),
        retries = item.retries.toString(),
        source = item.source,
        approvedText = "${item.approved}",
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationPlanViews(items: MutableList<ViolationPlan>): MutableList<ViolationPlanView> {
    val views = mutableListOf<ViolationPlanView>()
    for (item in items) {
        views.add(toViolationPlanView(item))
    }
    return views
}

fun sumViolationPlanView(view: ViolationPlanView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedUpdatedAt.toDouble() + parsedDistanceKm + parsedRetries.toDouble()
}
