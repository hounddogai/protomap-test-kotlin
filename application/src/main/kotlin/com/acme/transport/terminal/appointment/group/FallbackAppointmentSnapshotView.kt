package com.acme.transport.terminal.appointment.group

/** How screens and exports show a fallback appointment snapshot. */
data class FallbackAppointmentSnapshotView(
    var code: String,
    var labelText: String,
    var durationMinutes: String,
    var notes: String,
    var attempts: String,
    var title: String,
    var rateText: String,
    var revisionText: String,
    var threshold: String,
    var statusLabel: String,
)

fun toFallbackAppointmentSnapshotView(item: FallbackAppointmentSnapshot): FallbackAppointmentSnapshotView {
    return FallbackAppointmentSnapshotView(
        code = item.code,
        labelText = item.label,
        durationMinutes = item.durationMinutes.toString(),
        notes = item.notes,
        attempts = item.attempts.toString(),
        title = item.title,
        rateText = "%.2f".format(item.rate),
        revisionText = item.revision.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAppointmentSnapshotViews(items: MutableList<FallbackAppointmentSnapshot>): MutableList<FallbackAppointmentSnapshotView> {
    val views = mutableListOf<FallbackAppointmentSnapshotView>()
    for (item in items) {
        views.add(toFallbackAppointmentSnapshotView(item))
    }
    return views
}

fun sumFallbackAppointmentSnapshotView(view: FallbackAppointmentSnapshotView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedAttempts.toDouble() + parsedRate
}
