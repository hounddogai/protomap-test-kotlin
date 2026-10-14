package com.acme.transport.network.hub.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled hub audit. */
data class ScheduledHubAuditView(
    var code: String,
    var label: String,
    var routeCodeText: String,
    var pinnedText: String,
    var externalRef: String,
    var threshold: String,
    var statusLabel: String,
)

fun toScheduledHubAuditView(item: ScheduledHubAudit): ScheduledHubAuditView {
    return ScheduledHubAuditView(
        code = item.code,
        label = truncate(item.label, 80),
        routeCodeText = item.routeCode,
        pinnedText = "${item.pinned}",
        externalRef = item.externalRef,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHubAuditViews(items: MutableList<ScheduledHubAudit>): MutableList<ScheduledHubAuditView> {
    val views = mutableListOf<ScheduledHubAuditView>()
    for (item in items) {
        views.add(toScheduledHubAuditView(item))
    }
    return views
}

fun sumScheduledHubAuditView(view: ScheduledHubAuditView): Double {
    val parsedThreshold = view.threshold.toDouble()
    return parsedThreshold
}
