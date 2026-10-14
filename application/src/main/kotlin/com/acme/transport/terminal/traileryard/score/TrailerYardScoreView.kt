package com.acme.transport.terminal.traileryard.score

/** How screens and exports show a trailer yard score. */
data class TrailerYardScoreView(
    var code: String,
    var title: String,
    var ratio: String,
    var maximumText: String,
    var phone: String,
    var floorValue: String,
    var shiftCode: String,
    var attempts: String,
    var statusLabel: String,
)

fun toTrailerYardScoreView(item: TrailerYardScore): TrailerYardScoreView {
    return TrailerYardScoreView(
        code = item.code,
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        maximumText = "%.2f".format(item.maximum),
        phone = item.phone,
        floorValue = "%.2f".format(item.floorValue),
        shiftCode = item.shiftCode,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardScoreViews(items: MutableList<TrailerYardScore>): MutableList<TrailerYardScoreView> {
    val views = mutableListOf<TrailerYardScoreView>()
    for (item in items) {
        views.add(toTrailerYardScoreView(item))
    }
    return views
}

fun sumTrailerYardScoreView(view: TrailerYardScoreView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedRatio + parsedMaximum + parsedFloorValue
}
