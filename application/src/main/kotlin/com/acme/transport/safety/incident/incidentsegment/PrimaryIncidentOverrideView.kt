package com.acme.transport.safety.incident.incidentsegment

/** How screens and exports show a primary incident override. */
data class PrimaryIncidentOverrideView(
    var codeText: String,
    var title: String,
    var ceilingText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toPrimaryIncidentOverrideView(item: PrimaryIncidentOverride): PrimaryIncidentOverrideView {
    return PrimaryIncidentOverrideView(
        codeText = item.code,
        title = item.title,
        ceilingText = "%.2f".format(item.ceiling),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryIncidentOverrideViews(items: MutableList<PrimaryIncidentOverride>): MutableList<PrimaryIncidentOverrideView> {
    val views = mutableListOf<PrimaryIncidentOverrideView>()
    for (item in items) {
        views.add(toPrimaryIncidentOverrideView(item))
    }
    return views
}

fun sumPrimaryIncidentOverrideView(view: PrimaryIncidentOverrideView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCeiling + parsedEtaMinutes.toDouble()
}
