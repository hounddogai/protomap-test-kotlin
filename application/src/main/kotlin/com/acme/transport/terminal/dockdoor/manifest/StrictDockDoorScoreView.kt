package com.acme.transport.terminal.dockdoor.manifest

/** How screens and exports show a strict dock door score. */
data class StrictDockDoorScoreView(
    var code: String,
    var label: String,
    var scoreText: String,
    var odometer: String,
    var attempts: String,
    var windowDays: String,
    var updatedAtText: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toStrictDockDoorScoreView(item: StrictDockDoorScore): StrictDockDoorScoreView {
    return StrictDockDoorScoreView(
        code = item.code,
        label = item.label,
        scoreText = "%.2f".format(item.score),
        odometer = item.odometer.toString(),
        attempts = item.attempts.toString(),
        windowDays = item.windowDays.toString(),
        updatedAtText = item.updatedAt.toString(),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDockDoorScoreViews(items: MutableList<StrictDockDoorScore>): MutableList<StrictDockDoorScoreView> {
    val views = mutableListOf<StrictDockDoorScoreView>()
    for (item in items) {
        views.add(toStrictDockDoorScoreView(item))
    }
    return views
}

fun sumStrictDockDoorScoreView(view: StrictDockDoorScoreView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedScore + parsedOdometer.toDouble() + parsedAttempts.toDouble()
}
