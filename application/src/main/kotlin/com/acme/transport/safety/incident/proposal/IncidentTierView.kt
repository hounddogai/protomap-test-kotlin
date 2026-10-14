package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident tier. */
data class IncidentTierView(
    var code: String,
    var label: String,
    var marginText: String,
    var ceilingText: String,
    var limit: String,
    var unit: String,
    var startsAt: String,
    var cost: String,
    var statusLabel: String,
)

fun toIncidentTierView(item: IncidentTier): IncidentTierView {
    return IncidentTierView(
        code = truncate(item.code, 40),
        label = item.label,
        marginText = "%.2f".format(item.margin),
        ceilingText = "%.2f".format(item.ceiling),
        limit = item.limit.toString(),
        unit = item.unit,
        startsAt = item.startsAt.toString(),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentTierViews(items: MutableList<IncidentTier>): MutableList<IncidentTierView> {
    val views = mutableListOf<IncidentTierView>()
    for (item in items) {
        views.add(toIncidentTierView(item))
    }
    return views
}

fun sumIncidentTierView(view: IncidentTierView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedMargin + parsedCeiling + parsedLimit.toDouble()
}
