package com.acme.transport.customs.hold.adjustment

/** How screens and exports show a hold score. */
data class HoldScoreView(
    var code: String,
    var name: String,
    var pinned: String,
    var routeCode: String,
    var attempts: String,
    var statusLabel: String,
)

fun toHoldScoreView(item: HoldScore): HoldScoreView {
    return HoldScoreView(
        code = item.code,
        name = item.name,
        pinned = "${item.pinned}",
        routeCode = item.routeCode,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldScoreViews(items: MutableList<HoldScore>): MutableList<HoldScoreView> {
    val views = mutableListOf<HoldScoreView>()
    for (item in items) {
        views.add(toHoldScoreView(item))
    }
    return views
}

fun sumHoldScoreView(view: HoldScoreView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
