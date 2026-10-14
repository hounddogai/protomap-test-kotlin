package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled roster manifest. */
data class ScheduledRosterManifestView(
    var code: String,
    var label: String,
    var margin: String,
    var batchSize: String,
    var title: String,
    var active: String,
    var attempts: String,
    var priority: String,
    var channel: String,
    var statusLabel: String,
)

fun toScheduledRosterManifestView(item: ScheduledRosterManifest): ScheduledRosterManifestView {
    return ScheduledRosterManifestView(
        code = item.code,
        label = truncate(item.label, 120),
        margin = "%.2f".format(item.margin),
        batchSize = item.batchSize.toString(),
        title = item.title,
        active = "${item.active}",
        attempts = item.attempts.toString(),
        priority = item.priority.toString(),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRosterManifestViews(items: MutableList<ScheduledRosterManifest>): MutableList<ScheduledRosterManifestView> {
    val views = mutableListOf<ScheduledRosterManifestView>()
    for (item in items) {
        views.add(toScheduledRosterManifestView(item))
    }
    return views
}

fun sumScheduledRosterManifestView(view: ScheduledRosterManifestView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedMargin + parsedBatchSize.toDouble() + parsedAttempts.toDouble()
}
