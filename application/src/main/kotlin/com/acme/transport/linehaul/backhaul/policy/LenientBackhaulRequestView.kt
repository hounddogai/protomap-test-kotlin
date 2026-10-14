package com.acme.transport.linehaul.backhaul.policy

/** How screens and exports show a lenient backhaul request. */
data class LenientBackhaulRequestView(
    var code: String,
    var name: String,
    var visibleText: String,
    var driverIdText: String,
    var dock: String,
    var countryText: String,
    var durationMinutes: String,
    var minimum: String,
    var trailerIdText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toLenientBackhaulRequestView(item: LenientBackhaulRequest): LenientBackhaulRequestView {
    return LenientBackhaulRequestView(
        code = item.code,
        name = item.name,
        visibleText = "${item.visible}",
        driverIdText = item.driverId,
        dock = item.dock,
        countryText = item.country,
        durationMinutes = item.durationMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        trailerIdText = item.trailerId,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientBackhaulRequestViews(items: MutableList<LenientBackhaulRequest>): MutableList<LenientBackhaulRequestView> {
    val views = mutableListOf<LenientBackhaulRequestView>()
    for (item in items) {
        views.add(toLenientBackhaulRequestView(item))
    }
    return views
}

fun sumLenientBackhaulRequestView(view: LenientBackhaulRequestView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedMinimum + parsedScore
}
