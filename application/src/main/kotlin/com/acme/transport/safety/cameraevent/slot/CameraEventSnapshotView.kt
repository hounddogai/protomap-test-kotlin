package com.acme.transport.safety.cameraevent.slot

/** How screens and exports show a camera event snapshot. */
data class CameraEventSnapshotView(
    var code: String,
    var title: String,
    var durationMinutesText: String,
    var attempts: String,
    var vehicleId: String,
    var depot: String,
    var retries: String,
    var budget: String,
    var sequence: String,
    var statusLabel: String,
)

fun toCameraEventSnapshotView(item: CameraEventSnapshot): CameraEventSnapshotView {
    return CameraEventSnapshotView(
        code = item.code,
        title = item.title,
        durationMinutesText = item.durationMinutes.toString(),
        attempts = item.attempts.toString(),
        vehicleId = item.vehicleId,
        depot = item.depot,
        retries = item.retries.toString(),
        budget = "%.2f".format(item.budget),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventSnapshotViews(items: MutableList<CameraEventSnapshot>): MutableList<CameraEventSnapshotView> {
    val views = mutableListOf<CameraEventSnapshotView>()
    for (item in items) {
        views.add(toCameraEventSnapshotView(item))
    }
    return views
}

fun sumCameraEventSnapshotView(view: CameraEventSnapshotView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedDurationMinutes.toDouble() + parsedAttempts.toDouble() + parsedRetries.toDouble()
}
