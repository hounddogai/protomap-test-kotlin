package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area threshold. */
data class ServiceAreaThresholdView(
    var code: String,
    var name: String,
    var windowDaysText: String,
    var retries: String,
    var routeCodeText: String,
    var createdAtText: String,
    var limit: String,
    var statusLabel: String,
)

fun toServiceAreaThresholdView(item: ServiceAreaThreshold): ServiceAreaThresholdView {
    return ServiceAreaThresholdView(
        code = truncate(item.code, 40),
        name = item.name,
        windowDaysText = item.windowDays.toString(),
        retries = item.retries.toString(),
        routeCodeText = truncate(item.routeCode, 40),
        createdAtText = item.createdAt.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaThresholdViews(items: MutableList<ServiceAreaThreshold>): MutableList<ServiceAreaThresholdView> {
    val views = mutableListOf<ServiceAreaThresholdView>()
    for (item in items) {
        views.add(toServiceAreaThresholdView(item))
    }
    return views
}

fun sumServiceAreaThresholdView(view: ServiceAreaThresholdView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedWindowDays.toDouble() + parsedRetries.toDouble() + parsedCreatedAt.toDouble()
}
