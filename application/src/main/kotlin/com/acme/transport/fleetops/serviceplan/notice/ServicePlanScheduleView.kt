package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan schedule. */
data class ServicePlanScheduleView(
    var codeText: String,
    var name: String,
    var archived: String,
    var localeText: String,
    var attempts: String,
    var distanceKm: String,
    var shiftCode: String,
    var weight: String,
    var carrier: String,
    var titleText: String,
    var channel: String,
    var statusLabel: String,
)

fun toServicePlanScheduleView(item: ServicePlanSchedule): ServicePlanScheduleView {
    return ServicePlanScheduleView(
        codeText = item.code,
        name = truncate(item.name, 80),
        archived = "${item.archived}",
        localeText = item.locale,
        attempts = item.attempts.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        shiftCode = item.shiftCode,
        weight = "%.2f".format(item.weight),
        carrier = item.carrier,
        titleText = truncate(item.title, 120),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanScheduleViews(items: MutableList<ServicePlanSchedule>): MutableList<ServicePlanScheduleView> {
    val views = mutableListOf<ServicePlanScheduleView>()
    for (item in items) {
        views.add(toServicePlanScheduleView(item))
    }
    return views
}

fun sumServicePlanScheduleView(view: ServicePlanScheduleView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedAttempts.toDouble() + parsedDistanceKm + parsedWeight
}
