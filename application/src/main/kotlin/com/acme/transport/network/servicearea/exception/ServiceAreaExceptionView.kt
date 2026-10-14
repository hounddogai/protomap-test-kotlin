package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area exception. */
data class ServiceAreaExceptionView(
    var code: String,
    var titleText: String,
    var retries: String,
    var minimum: String,
    var currencyText: String,
    var regionText: String,
    var cost: String,
    var version: String,
    var startsAtText: String,
    var pinnedText: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toServiceAreaExceptionView(item: ServiceAreaException): ServiceAreaExceptionView {
    return ServiceAreaExceptionView(
        code = item.code,
        titleText = truncate(item.title, 40),
        retries = item.retries.toString(),
        minimum = "%.2f".format(item.minimum),
        currencyText = truncate(item.currency, 40),
        regionText = item.region,
        cost = "%.2f".format(item.cost),
        version = item.version.toString(),
        startsAtText = item.startsAt.toString(),
        pinnedText = "${item.pinned}",
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaExceptionViews(items: MutableList<ServiceAreaException>): MutableList<ServiceAreaExceptionView> {
    val views = mutableListOf<ServiceAreaExceptionView>()
    for (item in items) {
        views.add(toServiceAreaExceptionView(item))
    }
    return views
}

fun sumServiceAreaExceptionView(view: ServiceAreaExceptionView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedRetries.toDouble() + parsedMinimum + parsedCost
}
