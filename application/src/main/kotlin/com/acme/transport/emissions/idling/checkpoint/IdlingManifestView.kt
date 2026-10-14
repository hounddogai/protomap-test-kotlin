package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling manifest. */
data class IdlingManifestView(
    var code: String,
    var title: String,
    var revision: String,
    var priorityText: String,
    var active: String,
    var locale: String,
    var checksum: String,
    var timezone: String,
    var windowDays: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toIdlingManifestView(item: IdlingManifest): IdlingManifestView {
    return IdlingManifestView(
        code = truncate(item.code, 40),
        title = item.title,
        revision = item.revision.toString(),
        priorityText = item.priority.toString(),
        active = "${item.active}",
        locale = item.locale,
        checksum = truncate(item.checksum, 40),
        timezone = item.timezone,
        windowDays = item.windowDays.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingManifestViews(items: MutableList<IdlingManifest>): MutableList<IdlingManifestView> {
    val views = mutableListOf<IdlingManifestView>()
    for (item in items) {
        views.add(toIdlingManifestView(item))
    }
    return views
}

fun sumIdlingManifestView(view: IdlingManifestView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedRevision.toDouble() + parsedPriority.toDouble() + parsedWindowDays.toDouble()
}
