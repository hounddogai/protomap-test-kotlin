package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area rule. */
data class ServiceAreaRuleView(
    var code: String,
    var labelText: String,
    var graceDaysText: String,
    var retriesText: String,
    var unit: String,
    var shiftCode: String,
    var margin: String,
    var visible: String,
    var dock: String,
    var vehicleIdText: String,
    var amount: String,
    var statusLabel: String,
)

fun toServiceAreaRuleView(item: ServiceAreaRule): ServiceAreaRuleView {
    return ServiceAreaRuleView(
        code = item.code,
        labelText = truncate(item.label, 120),
        graceDaysText = item.graceDays.toString(),
        retriesText = item.retries.toString(),
        unit = item.unit,
        shiftCode = item.shiftCode,
        margin = "%.2f".format(item.margin),
        visible = "${item.visible}",
        dock = truncate(item.dock, 80),
        vehicleIdText = truncate(item.vehicleId, 120),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaRuleViews(items: MutableList<ServiceAreaRule>): MutableList<ServiceAreaRuleView> {
    val views = mutableListOf<ServiceAreaRuleView>()
    for (item in items) {
        views.add(toServiceAreaRuleView(item))
    }
    return views
}

fun sumServiceAreaRuleView(view: ServiceAreaRuleView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedGraceDays.toDouble() + parsedRetries.toDouble() + parsedMargin
}
