package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident policy. */
data class IncidentPolicyView(
    var code: String,
    var name: String,
    var timezone: String,
    var minimumText: String,
    var laneText: String,
    var startsAt: String,
    var externalRef: String,
    var loadKgText: String,
    var dock: String,
    var depot: String,
    var region: String,
    var statusLabel: String,
)

fun toIncidentPolicyView(item: IncidentPolicy): IncidentPolicyView {
    return IncidentPolicyView(
        code = item.code,
        name = item.name,
        timezone = item.timezone,
        minimumText = "%.2f".format(item.minimum),
        laneText = truncate(item.lane, 120),
        startsAt = item.startsAt.toString(),
        externalRef = truncate(item.externalRef, 80),
        loadKgText = "%.2f".format(item.loadKg),
        dock = item.dock,
        depot = truncate(item.depot, 80),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentPolicyViews(items: MutableList<IncidentPolicy>): MutableList<IncidentPolicyView> {
    val views = mutableListOf<IncidentPolicyView>()
    for (item in items) {
        views.add(toIncidentPolicyView(item))
    }
    return views
}

fun sumIncidentPolicyView(view: IncidentPolicyView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedMinimum + parsedStartsAt.toDouble() + parsedLoadKg
}
