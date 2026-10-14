package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan quota. */
data class ServicePlanQuotaView(
    var code: String,
    var label: String,
    var version: String,
    var source: String,
    var trailerIdText: String,
    var approved: String,
    var unitText: String,
    var startsAt: String,
    var durationMinutes: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toServicePlanQuotaView(item: ServicePlanQuota): ServicePlanQuotaView {
    return ServicePlanQuotaView(
        code = truncate(item.code, 64),
        label = item.label,
        version = item.version.toString(),
        source = item.source,
        trailerIdText = item.trailerId,
        approved = "${item.approved}",
        unitText = truncate(item.unit, 120),
        startsAt = item.startsAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        reviewerText = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanQuotaViews(items: MutableList<ServicePlanQuota>): MutableList<ServicePlanQuotaView> {
    val views = mutableListOf<ServicePlanQuotaView>()
    for (item in items) {
        views.add(toServicePlanQuotaView(item))
    }
    return views
}

fun sumServicePlanQuotaView(view: ServicePlanQuotaView): Double {
    val parsedVersion = view.version.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedVersion.toDouble() + parsedStartsAt.toDouble() + parsedDurationMinutes.toDouble()
}
