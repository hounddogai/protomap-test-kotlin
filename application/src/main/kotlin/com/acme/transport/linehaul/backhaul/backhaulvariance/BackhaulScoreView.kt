package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul score. */
data class BackhaulScoreView(
    var code: String,
    var label: String,
    var odometerText: String,
    var floorValue: String,
    var channel: String,
    var statusLabel: String,
)

fun toBackhaulScoreView(item: BackhaulScore): BackhaulScoreView {
    return BackhaulScoreView(
        code = truncate(item.code, 80),
        label = item.label,
        odometerText = item.odometer.toString(),
        floorValue = "%.2f".format(item.floorValue),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulScoreViews(items: MutableList<BackhaulScore>): MutableList<BackhaulScoreView> {
    val views = mutableListOf<BackhaulScoreView>()
    for (item in items) {
        views.add(toBackhaulScoreView(item))
    }
    return views
}

fun sumBackhaulScoreView(view: BackhaulScoreView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedOdometer.toDouble() + parsedFloorValue
}
