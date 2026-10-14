package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service plan request. */
data class FallbackServicePlanRequestView(
    var codeText: String,
    var name: String,
    var contactEmail: String,
    var priority: String,
    var driverIdText: String,
    var archivedText: String,
    var retries: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toFallbackServicePlanRequestView(item: FallbackServicePlanRequest): FallbackServicePlanRequestView {
    return FallbackServicePlanRequestView(
        codeText = truncate(item.code, 40),
        name = item.name,
        contactEmail = truncate(item.contactEmail, 120),
        priority = item.priority.toString(),
        driverIdText = item.driverId,
        archivedText = "${item.archived}",
        retries = item.retries.toString(),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServicePlanRequestViews(items: MutableList<FallbackServicePlanRequest>): MutableList<FallbackServicePlanRequestView> {
    val views = mutableListOf<FallbackServicePlanRequestView>()
    for (item in items) {
        views.add(toFallbackServicePlanRequestView(item))
    }
    return views
}

fun sumFallbackServicePlanRequestView(view: FallbackServicePlanRequestView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedPriority.toDouble() + parsedRetries.toDouble()
}
