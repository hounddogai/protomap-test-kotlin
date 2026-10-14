package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

/** How screens and exports show a pending backhaul score. */
data class PendingBackhaulScoreView(
    var code: String,
    var title: String,
    var startsAt: String,
    var attempts: String,
    var active: String,
    var fuelLitersText: String,
    var capacity: String,
    var priority: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toPendingBackhaulScoreView(item: PendingBackhaulScore): PendingBackhaulScoreView {
    return PendingBackhaulScoreView(
        code = item.code,
        title = item.title,
        startsAt = item.startsAt.toString(),
        attempts = item.attempts.toString(),
        active = "${item.active}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        capacity = item.capacity.toString(),
        priority = item.priority.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingBackhaulScoreViews(items: MutableList<PendingBackhaulScore>): MutableList<PendingBackhaulScoreView> {
    val views = mutableListOf<PendingBackhaulScoreView>()
    for (item in items) {
        views.add(toPendingBackhaulScoreView(item))
    }
    return views
}

fun sumPendingBackhaulScoreView(view: PendingBackhaulScoreView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedStartsAt.toDouble() + parsedAttempts.toDouble() + parsedFuelLiters
}
