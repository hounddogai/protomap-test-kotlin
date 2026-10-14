package com.acme.transport.customs.inspection.budget

/** How screens and exports show a inspection window. */
data class InspectionWindowView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var rate: String,
    var currency: String,
    var windowDays: String,
    var approvedText: String,
    var expiresAt: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toInspectionWindowView(item: InspectionWindow): InspectionWindowView {
    return InspectionWindowView(
        code = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        rate = "%.2f".format(item.rate),
        currency = item.currency,
        windowDays = item.windowDays.toString(),
        approvedText = "${item.approved}",
        expiresAt = item.expiresAt.toString(),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionWindowViews(items: MutableList<InspectionWindow>): MutableList<InspectionWindowView> {
    val views = mutableListOf<InspectionWindowView>()
    for (item in items) {
        views.add(toInspectionWindowView(item))
    }
    return views
}

fun sumInspectionWindowView(view: InspectionWindowView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedEtaMinutes.toDouble() + parsedRate + parsedWindowDays.toDouble()
}
