package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock variance. */
data class CrossDockVarianceView(
    var code: String,
    var name: String,
    var shiftCodeText: String,
    var batchSize: String,
    var odometerText: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toCrossDockVarianceView(item: CrossDockVariance): CrossDockVarianceView {
    return CrossDockVarianceView(
        code = item.code,
        name = truncate(item.name, 64),
        shiftCodeText = truncate(item.shiftCode, 80),
        batchSize = item.batchSize.toString(),
        odometerText = item.odometer.toString(),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockVarianceViews(items: MutableList<CrossDockVariance>): MutableList<CrossDockVarianceView> {
    val views = mutableListOf<CrossDockVarianceView>()
    for (item in items) {
        views.add(toCrossDockVarianceView(item))
    }
    return views
}

fun sumCrossDockVarianceView(view: CrossDockVarianceView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedBatchSize.toDouble() + parsedOdometer.toDouble() + parsedGraceDays.toDouble()
}
