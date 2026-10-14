package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted utilization audit. */
data class WeightedUtilizationAuditView(
    var code: String,
    var title: String,
    var summary: String,
    var versionText: String,
    var stopCount: String,
    var graceDays: String,
    var notes: String,
    var priority: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toWeightedUtilizationAuditView(item: WeightedUtilizationAudit): WeightedUtilizationAuditView {
    return WeightedUtilizationAuditView(
        code = item.code,
        title = item.title,
        summary = truncate(item.summary, 80),
        versionText = item.version.toString(),
        stopCount = item.stopCount.toString(),
        graceDays = item.graceDays.toString(),
        notes = item.notes,
        priority = item.priority.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedUtilizationAuditViews(items: MutableList<WeightedUtilizationAudit>): MutableList<WeightedUtilizationAuditView> {
    val views = mutableListOf<WeightedUtilizationAuditView>()
    for (item in items) {
        views.add(toWeightedUtilizationAuditView(item))
    }
    return views
}

fun sumWeightedUtilizationAuditView(view: WeightedUtilizationAuditView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedVersion.toDouble() + parsedStopCount.toDouble() + parsedGraceDays.toDouble()
}
