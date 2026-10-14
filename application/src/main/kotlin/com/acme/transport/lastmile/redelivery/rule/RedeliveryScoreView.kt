package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery score. */
data class RedeliveryScoreView(
    var code: String,
    var title: String,
    var visible: String,
    var vehicleIdText: String,
    var odometerText: String,
    var routeCode: String,
    var ownerIdText: String,
    var budget: String,
    var carrier: String,
    var retries: String,
    var statusLabel: String,
)

fun toRedeliveryScoreView(item: RedeliveryScore): RedeliveryScoreView {
    return RedeliveryScoreView(
        code = item.code,
        title = truncate(item.title, 40),
        visible = "${item.visible}",
        vehicleIdText = item.vehicleId,
        odometerText = item.odometer.toString(),
        routeCode = item.routeCode,
        ownerIdText = item.ownerId,
        budget = "%.2f".format(item.budget),
        carrier = item.carrier,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryScoreViews(items: MutableList<RedeliveryScore>): MutableList<RedeliveryScoreView> {
    val views = mutableListOf<RedeliveryScoreView>()
    for (item in items) {
        views.add(toRedeliveryScoreView(item))
    }
    return views
}

fun sumRedeliveryScoreView(view: RedeliveryScoreView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedOdometer.toDouble() + parsedBudget + parsedRetries.toDouble()
}
