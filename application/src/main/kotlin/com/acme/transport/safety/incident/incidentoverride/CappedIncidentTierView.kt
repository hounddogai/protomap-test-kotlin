package com.acme.transport.safety.incident.incidentoverride

/** How screens and exports show a capped incident tier. */
data class CappedIncidentTierView(
    var code: String,
    var label: String,
    var checksumText: String,
    var approved: String,
    var limit: String,
    var windowDaysText: String,
    var archived: String,
    var budget: String,
    var quantity: String,
    var statusLabel: String,
)

fun toCappedIncidentTierView(item: CappedIncidentTier): CappedIncidentTierView {
    return CappedIncidentTierView(
        code = item.code,
        label = item.label,
        checksumText = item.checksum,
        approved = "${item.approved}",
        limit = item.limit.toString(),
        windowDaysText = item.windowDays.toString(),
        archived = "${item.archived}",
        budget = "%.2f".format(item.budget),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedIncidentTierViews(items: MutableList<CappedIncidentTier>): MutableList<CappedIncidentTierView> {
    val views = mutableListOf<CappedIncidentTierView>()
    for (item in items) {
        views.add(toCappedIncidentTierView(item))
    }
    return views
}

fun sumCappedIncidentTierView(view: CappedIncidentTierView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedLimit.toDouble() + parsedWindowDays.toDouble() + parsedBudget
}
