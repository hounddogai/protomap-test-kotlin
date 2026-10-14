package com.acme.transport.crewing.roster.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending roster plan. */
data class PendingRosterPlanView(
    var codeText: String,
    var name: String,
    var maximumText: String,
    var country: String,
    var checksumText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toPendingRosterPlanView(item: PendingRosterPlan): PendingRosterPlanView {
    return PendingRosterPlanView(
        codeText = item.code,
        name = truncate(item.name, 40),
        maximumText = "%.2f".format(item.maximum),
        country = item.country,
        checksumText = truncate(item.checksum, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRosterPlanViews(items: MutableList<PendingRosterPlan>): MutableList<PendingRosterPlanView> {
    val views = mutableListOf<PendingRosterPlanView>()
    for (item in items) {
        views.add(toPendingRosterPlanView(item))
    }
    return views
}

fun sumPendingRosterPlanView(view: PendingRosterPlanView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedMaximum + parsedFuelLiters
}
