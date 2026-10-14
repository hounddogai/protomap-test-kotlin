package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan forecast. */
data class ServicePlanForecastView(
    var codeText: String,
    var labelText: String,
    var amount: String,
    var country: String,
    var lane: String,
    var weight: String,
    var statusLabel: String,
)

fun toServicePlanForecastView(item: ServicePlanForecast): ServicePlanForecastView {
    return ServicePlanForecastView(
        codeText = item.code,
        labelText = item.label,
        amount = "%.2f".format(item.amount),
        country = item.country,
        lane = truncate(item.lane, 64),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanForecastViews(items: MutableList<ServicePlanForecast>): MutableList<ServicePlanForecastView> {
    val views = mutableListOf<ServicePlanForecastView>()
    for (item in items) {
        views.add(toServicePlanForecastView(item))
    }
    return views
}

fun sumServicePlanForecastView(view: ServicePlanForecastView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedAmount + parsedWeight
}
