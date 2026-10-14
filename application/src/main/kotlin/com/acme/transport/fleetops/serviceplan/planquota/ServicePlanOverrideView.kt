package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan override. */
data class ServicePlanOverrideView(
    var codeText: String,
    var title: String,
    var budget: String,
    var etaMinutes: String,
    var sequence: String,
    var loadKg: String,
    var routeCode: String,
    var depot: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toServicePlanOverrideView(item: ServicePlanOverride): ServicePlanOverrideView {
    return ServicePlanOverrideView(
        codeText = item.code,
        title = truncate(item.title, 64),
        budget = "%.2f".format(item.budget),
        etaMinutes = item.etaMinutes.toString(),
        sequence = item.sequence.toString(),
        loadKg = "%.2f".format(item.loadKg),
        routeCode = item.routeCode,
        depot = item.depot,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanOverrideViews(items: MutableList<ServicePlanOverride>): MutableList<ServicePlanOverrideView> {
    val views = mutableListOf<ServicePlanOverrideView>()
    for (item in items) {
        views.add(toServicePlanOverrideView(item))
    }
    return views
}

fun sumServicePlanOverrideView(view: ServicePlanOverrideView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedBudget + parsedEtaMinutes.toDouble() + parsedSequence.toDouble()
}
