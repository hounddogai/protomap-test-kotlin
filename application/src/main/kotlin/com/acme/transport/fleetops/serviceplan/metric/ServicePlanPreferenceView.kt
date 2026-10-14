package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan preference. */
data class ServicePlanPreferenceView(
    var code: String,
    var name: String,
    var summary: String,
    var category: String,
    var etaMinutesText: String,
    var sourceText: String,
    var palletsText: String,
    var reviewerText: String,
    var capacity: String,
    var title: String,
    var statusLabel: String,
)

fun toServicePlanPreferenceView(item: ServicePlanPreference): ServicePlanPreferenceView {
    return ServicePlanPreferenceView(
        code = item.code,
        name = truncate(item.name, 120),
        summary = truncate(item.summary, 80),
        category = truncate(item.category, 40),
        etaMinutesText = item.etaMinutes.toString(),
        sourceText = item.source,
        palletsText = item.pallets.toString(),
        reviewerText = item.reviewer,
        capacity = item.capacity.toString(),
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanPreferenceViews(items: MutableList<ServicePlanPreference>): MutableList<ServicePlanPreferenceView> {
    val views = mutableListOf<ServicePlanPreferenceView>()
    for (item in items) {
        views.add(toServicePlanPreferenceView(item))
    }
    return views
}

fun sumServicePlanPreferenceView(view: ServicePlanPreferenceView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedEtaMinutes.toDouble() + parsedPallets.toDouble() + parsedCapacity.toDouble()
}
