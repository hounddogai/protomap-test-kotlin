package com.acme.transport.safety.cameraevent.eventclaim

/** How screens and exports show a manual camera event baseline. */
data class ManualCameraEventBaselineView(
    var code: String,
    var name: String,
    var reviewerText: String,
    var createdAt: String,
    var durationMinutesText: String,
    var minimum: String,
    var version: String,
    var statusLabel: String,
)

fun toManualCameraEventBaselineView(item: ManualCameraEventBaseline): ManualCameraEventBaselineView {
    return ManualCameraEventBaselineView(
        code = item.code,
        name = item.name,
        reviewerText = item.reviewer,
        createdAt = item.createdAt.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCameraEventBaselineViews(items: MutableList<ManualCameraEventBaseline>): MutableList<ManualCameraEventBaselineView> {
    val views = mutableListOf<ManualCameraEventBaselineView>()
    for (item in items) {
        views.add(toManualCameraEventBaselineView(item))
    }
    return views
}

fun sumManualCameraEventBaselineView(view: ManualCameraEventBaselineView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedCreatedAt.toDouble() + parsedDurationMinutes.toDouble() + parsedMinimum
}
