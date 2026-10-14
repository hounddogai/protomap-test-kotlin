package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan summary. */
data class ServicePlanSummaryView(
    var code: String,
    var name: String,
    var pinned: String,
    var ceiling: String,
    var notes: String,
    var phone: String,
    var summary: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toServicePlanSummaryView(item: ServicePlanSummary): ServicePlanSummaryView {
    return ServicePlanSummaryView(
        code = item.code,
        name = item.name,
        pinned = "${item.pinned}",
        ceiling = "%.2f".format(item.ceiling),
        notes = item.notes,
        phone = truncate(item.phone, 64),
        summary = truncate(item.summary, 120),
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanSummaryViews(items: MutableList<ServicePlanSummary>): MutableList<ServicePlanSummaryView> {
    val views = mutableListOf<ServicePlanSummaryView>()
    for (item in items) {
        views.add(toServicePlanSummaryView(item))
    }
    return views
}

fun sumServicePlanSummaryView(view: ServicePlanSummaryView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedCeiling + parsedLoadKg
}
