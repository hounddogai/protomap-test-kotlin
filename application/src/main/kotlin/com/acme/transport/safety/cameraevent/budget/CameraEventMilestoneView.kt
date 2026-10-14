package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event milestone. */
data class CameraEventMilestoneView(
    var code: String,
    var title: String,
    var pinnedText: String,
    var country: String,
    var dock: String,
    var trailerId: String,
    var threshold: String,
    var timezone: String,
    var statusLabel: String,
)

fun toCameraEventMilestoneView(item: CameraEventMilestone): CameraEventMilestoneView {
    return CameraEventMilestoneView(
        code = item.code,
        title = item.title,
        pinnedText = "${item.pinned}",
        country = truncate(item.country, 80),
        dock = item.dock,
        trailerId = item.trailerId,
        threshold = "%.2f".format(item.threshold),
        timezone = truncate(item.timezone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventMilestoneViews(items: MutableList<CameraEventMilestone>): MutableList<CameraEventMilestoneView> {
    val views = mutableListOf<CameraEventMilestoneView>()
    for (item in items) {
        views.add(toCameraEventMilestoneView(item))
    }
    return views
}

fun sumCameraEventMilestoneView(view: CameraEventMilestoneView): Double {
    val parsedThreshold = view.threshold.toDouble()
    return parsedThreshold
}
