package com.acme.transport.freightpay.accessorial.policy

/** How screens and exports show a accessorial score. */
data class AccessorialScoreView(
    var code: String,
    var label: String,
    var floorValue: String,
    var sourceText: String,
    var score: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toAccessorialScoreView(item: AccessorialScore): AccessorialScoreView {
    return AccessorialScoreView(
        code = item.code,
        label = item.label,
        floorValue = "%.2f".format(item.floorValue),
        sourceText = item.source,
        score = "%.2f".format(item.score),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialScoreViews(items: MutableList<AccessorialScore>): MutableList<AccessorialScoreView> {
    val views = mutableListOf<AccessorialScoreView>()
    for (item in items) {
        views.add(toAccessorialScoreView(item))
    }
    return views
}

fun sumAccessorialScoreView(view: AccessorialScoreView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedFloorValue + parsedScore + parsedEtaMinutes.toDouble()
}
