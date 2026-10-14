package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident claim. */
data class IncidentClaimView(
    var code: String,
    var label: String,
    var retries: String,
    var loadKg: String,
    var weight: String,
    var depotText: String,
    var statusLabel: String,
)

fun toIncidentClaimView(item: IncidentClaim): IncidentClaimView {
    return IncidentClaimView(
        code = truncate(item.code, 64),
        label = item.label,
        retries = item.retries.toString(),
        loadKg = "%.2f".format(item.loadKg),
        weight = "%.2f".format(item.weight),
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentClaimViews(items: MutableList<IncidentClaim>): MutableList<IncidentClaimView> {
    val views = mutableListOf<IncidentClaimView>()
    for (item in items) {
        views.add(toIncidentClaimView(item))
    }
    return views
}

fun sumIncidentClaimView(view: IncidentClaimView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedRetries.toDouble() + parsedLoadKg + parsedWeight
}
