package com.acme.transport.safety.cameraevent.globaleventaudit

/** How screens and exports show a camera event schedule. */
data class CameraEventScheduleView(
    var code: String,
    var label: String,
    var durationMinutesText: String,
    var endsAtText: String,
    var dockText: String,
    var laneText: String,
    var stopCount: String,
    var etaMinutesText: String,
    var segmentText: String,
    var retries: String,
    var statusLabel: String,
)

fun toCameraEventScheduleView(item: CameraEventSchedule): CameraEventScheduleView {
    return CameraEventScheduleView(
        code = item.code,
        label = item.label,
        durationMinutesText = item.durationMinutes.toString(),
        endsAtText = item.endsAt.toString(),
        dockText = item.dock,
        laneText = item.lane,
        stopCount = item.stopCount.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        segmentText = item.segment,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventScheduleViews(items: MutableList<CameraEventSchedule>): MutableList<CameraEventScheduleView> {
    val views = mutableListOf<CameraEventScheduleView>()
    for (item in items) {
        views.add(toCameraEventScheduleView(item))
    }
    return views
}

fun sumCameraEventScheduleView(view: CameraEventScheduleView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble() + parsedStopCount.toDouble()
}
