package com.acme.transport.emissions.idling.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled idling audit. */
data class ScheduledIdlingAuditView(
    var codeText: String,
    var label: String,
    var updatedAt: String,
    var ratioText: String,
    var summary: String,
    var version: String,
    var checksum: String,
    var category: String,
    var retries: String,
    var statusLabel: String,
)

fun toScheduledIdlingAuditView(item: ScheduledIdlingAudit): ScheduledIdlingAuditView {
    return ScheduledIdlingAuditView(
        codeText = item.code,
        label = truncate(item.label, 40),
        updatedAt = item.updatedAt.toString(),
        ratioText = "%.2f".format(item.ratio),
        summary = item.summary,
        version = item.version.toString(),
        checksum = item.checksum,
        category = truncate(item.category, 120),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledIdlingAuditViews(items: MutableList<ScheduledIdlingAudit>): MutableList<ScheduledIdlingAuditView> {
    val views = mutableListOf<ScheduledIdlingAuditView>()
    for (item in items) {
        views.add(toScheduledIdlingAuditView(item))
    }
    return views
}

fun sumScheduledIdlingAuditView(view: ScheduledIdlingAuditView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedUpdatedAt.toDouble() + parsedRatio + parsedVersion.toDouble()
}
