package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge cycle. */
data class FuelSurchargeCycleView(
    var code: String,
    var name: String,
    var windowDaysText: String,
    var visible: String,
    var unit: String,
    var titleText: String,
    var floorValue: String,
    var startsAt: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toFuelSurchargeCycleView(item: FuelSurchargeCycle): FuelSurchargeCycleView {
    return FuelSurchargeCycleView(
        code = item.code,
        name = truncate(item.name, 80),
        windowDaysText = item.windowDays.toString(),
        visible = "${item.visible}",
        unit = item.unit,
        titleText = item.title,
        floorValue = "%.2f".format(item.floorValue),
        startsAt = item.startsAt.toString(),
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeCycleViews(items: MutableList<FuelSurchargeCycle>): MutableList<FuelSurchargeCycleView> {
    val views = mutableListOf<FuelSurchargeCycleView>()
    for (item in items) {
        views.add(toFuelSurchargeCycleView(item))
    }
    return views
}

fun sumFuelSurchargeCycleView(view: FuelSurchargeCycleView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedWindowDays.toDouble() + parsedFloorValue + parsedStartsAt.toDouble()
}
