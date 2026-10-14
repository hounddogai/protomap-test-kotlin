package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global accessorial score. */
data class GlobalAccessorialScoreView(
    var code: String,
    var label: String,
    var stopCountText: String,
    var attemptsText: String,
    var ownerId: String,
    var name: String,
    var routeCode: String,
    var phone: String,
    var unit: String,
    var statusLabel: String,
)

fun toGlobalAccessorialScoreView(item: GlobalAccessorialScore): GlobalAccessorialScoreView {
    return GlobalAccessorialScoreView(
        code = item.code,
        label = truncate(item.label, 40),
        stopCountText = item.stopCount.toString(),
        attemptsText = item.attempts.toString(),
        ownerId = item.ownerId,
        name = item.name,
        routeCode = item.routeCode,
        phone = item.phone,
        unit = truncate(item.unit, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAccessorialScoreViews(items: MutableList<GlobalAccessorialScore>): MutableList<GlobalAccessorialScoreView> {
    val views = mutableListOf<GlobalAccessorialScoreView>()
    for (item in items) {
        views.add(toGlobalAccessorialScoreView(item))
    }
    return views
}

fun sumGlobalAccessorialScoreView(view: GlobalAccessorialScoreView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedStopCount.toDouble() + parsedAttempts.toDouble()
}
