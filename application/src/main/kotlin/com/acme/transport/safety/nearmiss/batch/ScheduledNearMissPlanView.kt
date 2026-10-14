package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled near miss plan. */
data class ScheduledNearMissPlanView(
    var codeText: String,
    var labelText: String,
    var margin: String,
    var routeCode: String,
    var etaMinutes: String,
    var archived: String,
    var statusLabel: String,
)

fun toScheduledNearMissPlanView(item: ScheduledNearMissPlan): ScheduledNearMissPlanView {
    return ScheduledNearMissPlanView(
        codeText = item.code,
        labelText = item.label,
        margin = "%.2f".format(item.margin),
        routeCode = truncate(item.routeCode, 120),
        etaMinutes = item.etaMinutes.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledNearMissPlanViews(items: MutableList<ScheduledNearMissPlan>): MutableList<ScheduledNearMissPlanView> {
    val views = mutableListOf<ScheduledNearMissPlanView>()
    for (item in items) {
        views.add(toScheduledNearMissPlanView(item))
    }
    return views
}

fun sumScheduledNearMissPlanView(view: ScheduledNearMissPlanView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedMargin + parsedEtaMinutes.toDouble()
}
