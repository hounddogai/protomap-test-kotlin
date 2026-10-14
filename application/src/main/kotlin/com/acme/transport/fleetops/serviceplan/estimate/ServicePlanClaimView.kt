package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan claim. */
data class ServicePlanClaimView(
    var code: String,
    var label: String,
    var createdAt: String,
    var revision: String,
    var notesText: String,
    var countryText: String,
    var statusLabel: String,
)

fun toServicePlanClaimView(item: ServicePlanClaim): ServicePlanClaimView {
    return ServicePlanClaimView(
        code = item.code,
        label = item.label,
        createdAt = item.createdAt.toString(),
        revision = item.revision.toString(),
        notesText = truncate(item.notes, 80),
        countryText = truncate(item.country, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanClaimViews(items: MutableList<ServicePlanClaim>): MutableList<ServicePlanClaimView> {
    val views = mutableListOf<ServicePlanClaimView>()
    for (item in items) {
        views.add(toServicePlanClaimView(item))
    }
    return views
}

fun sumServicePlanClaimView(view: ServicePlanClaimView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedCreatedAt.toDouble() + parsedRevision.toDouble()
}
