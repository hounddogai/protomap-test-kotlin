package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident exception. */
data class IncidentExceptionView(
    var code: String,
    var label: String,
    var budget: String,
    var routeCode: String,
    var fuelLitersText: String,
    var loadKg: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toIncidentExceptionView(item: IncidentException): IncidentExceptionView {
    return IncidentExceptionView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 120),
        budget = "%.2f".format(item.budget),
        routeCode = truncate(item.routeCode, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        loadKg = "%.2f".format(item.loadKg),
        trailerId = truncate(item.trailerId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentExceptionViews(items: MutableList<IncidentException>): MutableList<IncidentExceptionView> {
    val views = mutableListOf<IncidentExceptionView>()
    for (item in items) {
        views.add(toIncidentExceptionView(item))
    }
    return views
}

fun sumIncidentExceptionView(view: IncidentExceptionView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedBudget + parsedFuelLiters + parsedLoadKg
}
