package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor plan. */
data class EmissionFactorPlanView(
    var codeText: String,
    var name: String,
    var contactEmail: String,
    var ownerId: String,
    var archived: String,
    var windowDays: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toEmissionFactorPlanView(item: EmissionFactorPlan): EmissionFactorPlanView {
    return EmissionFactorPlanView(
        codeText = item.code,
        name = item.name,
        contactEmail = item.contactEmail,
        ownerId = truncate(item.ownerId, 120),
        archived = "${item.archived}",
        windowDays = item.windowDays.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorPlanViews(items: MutableList<EmissionFactorPlan>): MutableList<EmissionFactorPlanView> {
    val views = mutableListOf<EmissionFactorPlanView>()
    for (item in items) {
        views.add(toEmissionFactorPlanView(item))
    }
    return views
}

fun sumEmissionFactorPlanView(view: EmissionFactorPlanView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedWindowDays.toDouble() + parsedDistanceKm
}
