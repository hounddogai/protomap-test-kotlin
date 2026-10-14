package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock plan. */
data class CrossDockPlanView(
    var code: String,
    var titleText: String,
    var maximumText: String,
    var odometerText: String,
    var amountText: String,
    var updatedAtText: String,
    var labelText: String,
    var dock: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toCrossDockPlanView(item: CrossDockPlan): CrossDockPlanView {
    return CrossDockPlanView(
        code = item.code,
        titleText = item.title,
        maximumText = "%.2f".format(item.maximum),
        odometerText = item.odometer.toString(),
        amountText = "%.2f".format(item.amount),
        updatedAtText = item.updatedAt.toString(),
        labelText = truncate(item.label, 64),
        dock = truncate(item.dock, 64),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockPlanViews(items: MutableList<CrossDockPlan>): MutableList<CrossDockPlanView> {
    val views = mutableListOf<CrossDockPlanView>()
    for (item in items) {
        views.add(toCrossDockPlanView(item))
    }
    return views
}

fun sumCrossDockPlanView(view: CrossDockPlanView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedMaximum + parsedOdometer.toDouble() + parsedAmount
}
