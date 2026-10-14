package com.acme.transport.lastmile.drop.rollup

/** How screens and exports show a drop threshold. */
data class DropThresholdView(
    var code: String,
    var title: String,
    var startsAt: String,
    var approved: String,
    var windowDays: String,
    var odometerText: String,
    var floorValueText: String,
    var quantity: String,
    var loadKgText: String,
    var activeText: String,
    var statusLabel: String,
)

fun toDropThresholdView(item: DropThreshold): DropThresholdView {
    return DropThresholdView(
        code = item.code,
        title = item.title,
        startsAt = item.startsAt.toString(),
        approved = "${item.approved}",
        windowDays = item.windowDays.toString(),
        odometerText = item.odometer.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        quantity = item.quantity.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropThresholdViews(items: MutableList<DropThreshold>): MutableList<DropThresholdView> {
    val views = mutableListOf<DropThresholdView>()
    for (item in items) {
        views.add(toDropThresholdView(item))
    }
    return views
}

fun sumDropThresholdView(view: DropThresholdView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedStartsAt.toDouble() + parsedWindowDays.toDouble() + parsedOdometer.toDouble()
}
