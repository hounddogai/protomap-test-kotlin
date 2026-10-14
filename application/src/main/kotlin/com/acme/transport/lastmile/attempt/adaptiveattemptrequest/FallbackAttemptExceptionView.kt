package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

/** How screens and exports show a fallback attempt exception. */
data class FallbackAttemptExceptionView(
    var codeText: String,
    var label: String,
    var priority: String,
    var version: String,
    var scoreText: String,
    var notes: String,
    var capacity: String,
    var distanceKm: String,
    var etaMinutes: String,
    var expiresAtText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toFallbackAttemptExceptionView(item: FallbackAttemptException): FallbackAttemptExceptionView {
    return FallbackAttemptExceptionView(
        codeText = item.code,
        label = item.label,
        priority = item.priority.toString(),
        version = item.version.toString(),
        scoreText = "%.2f".format(item.score),
        notes = item.notes,
        capacity = item.capacity.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        etaMinutes = item.etaMinutes.toString(),
        expiresAtText = item.expiresAt.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAttemptExceptionViews(items: MutableList<FallbackAttemptException>): MutableList<FallbackAttemptExceptionView> {
    val views = mutableListOf<FallbackAttemptExceptionView>()
    for (item in items) {
        views.add(toFallbackAttemptExceptionView(item))
    }
    return views
}

fun sumFallbackAttemptExceptionView(view: FallbackAttemptExceptionView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedVersion = view.version.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedPriority.toDouble() + parsedVersion.toDouble() + parsedScore
}
