package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional availability plan. */
data class RegionalAvailabilityPlanView(
    var code: String,
    var nameText: String,
    var attempts: String,
    var reviewer: String,
    var priority: String,
    var contactEmail: String,
    var timezoneText: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toRegionalAvailabilityPlanView(item: RegionalAvailabilityPlan): RegionalAvailabilityPlanView {
    return RegionalAvailabilityPlanView(
        code = item.code,
        nameText = item.name,
        attempts = item.attempts.toString(),
        reviewer = item.reviewer,
        priority = item.priority.toString(),
        contactEmail = item.contactEmail,
        timezoneText = truncate(item.timezone, 64),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalAvailabilityPlanViews(items: MutableList<RegionalAvailabilityPlan>): MutableList<RegionalAvailabilityPlanView> {
    val views = mutableListOf<RegionalAvailabilityPlanView>()
    for (item in items) {
        views.add(toRegionalAvailabilityPlanView(item))
    }
    return views
}

fun sumRegionalAvailabilityPlanView(view: RegionalAvailabilityPlanView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedAttempts.toDouble() + parsedPriority.toDouble()
}
