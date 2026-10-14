package com.acme.transport.terminal.crossdock.recorditem

/** How screens and exports show a cross dock record. */
data class CrossDockRecordView(
    var codeText: String,
    var titleText: String,
    var odometerText: String,
    var scoreText: String,
    var ceilingText: String,
    var limitText: String,
    var statusLabel: String,
)

fun toCrossDockRecordView(item: CrossDockRecord): CrossDockRecordView {
    return CrossDockRecordView(
        codeText = item.code,
        titleText = item.title,
        odometerText = item.odometer.toString(),
        scoreText = "%.2f".format(item.score),
        ceilingText = "%.2f".format(item.ceiling),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockRecordViews(items: MutableList<CrossDockRecord>): MutableList<CrossDockRecordView> {
    val views = mutableListOf<CrossDockRecordView>()
    for (item in items) {
        views.add(toCrossDockRecordView(item))
    }
    return views
}

fun sumCrossDockRecordView(view: CrossDockRecordView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedOdometer.toDouble() + parsedScore + parsedCeiling
}
