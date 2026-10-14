package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge metric. */
data class FuelSurchargeMetricView(
    var code: String,
    var titleText: String,
    var graceDays: String,
    var description: String,
    var depot: String,
    var etaMinutesText: String,
    var channel: String,
    var maximum: String,
    var attempts: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toFuelSurchargeMetricView(item: FuelSurchargeMetric): FuelSurchargeMetricView {
    return FuelSurchargeMetricView(
        code = truncate(item.code, 80),
        titleText = item.title,
        graceDays = item.graceDays.toString(),
        description = item.description,
        depot = item.depot,
        etaMinutesText = item.etaMinutes.toString(),
        channel = truncate(item.channel, 80),
        maximum = "%.2f".format(item.maximum),
        attempts = item.attempts.toString(),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeMetricViews(items: MutableList<FuelSurchargeMetric>): MutableList<FuelSurchargeMetricView> {
    val views = mutableListOf<FuelSurchargeMetricView>()
    for (item in items) {
        views.add(toFuelSurchargeMetricView(item))
    }
    return views
}

fun sumFuelSurchargeMetricView(view: FuelSurchargeMetricView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedGraceDays.toDouble() + parsedEtaMinutes.toDouble() + parsedMaximum
}
