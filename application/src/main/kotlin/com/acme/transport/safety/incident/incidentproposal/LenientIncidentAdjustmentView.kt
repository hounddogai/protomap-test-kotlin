package com.acme.transport.safety.incident.incidentproposal

/** How screens and exports show a lenient incident adjustment. */
data class LenientIncidentAdjustmentView(
    var code: String,
    var name: String,
    var limit: String,
    var windowDays: String,
    var retries: String,
    var pinnedText: String,
    var floorValueText: String,
    var expiresAtText: String,
    var weight: String,
    var statusLabel: String,
)

fun toLenientIncidentAdjustmentView(item: LenientIncidentAdjustment): LenientIncidentAdjustmentView {
    return LenientIncidentAdjustmentView(
        code = item.code,
        name = item.name,
        limit = item.limit.toString(),
        windowDays = item.windowDays.toString(),
        retries = item.retries.toString(),
        pinnedText = "${item.pinned}",
        floorValueText = "%.2f".format(item.floorValue),
        expiresAtText = item.expiresAt.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientIncidentAdjustmentViews(items: MutableList<LenientIncidentAdjustment>): MutableList<LenientIncidentAdjustmentView> {
    val views = mutableListOf<LenientIncidentAdjustmentView>()
    for (item in items) {
        views.add(toLenientIncidentAdjustmentView(item))
    }
    return views
}

fun sumLenientIncidentAdjustmentView(view: LenientIncidentAdjustmentView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedLimit.toDouble() + parsedWindowDays.toDouble() + parsedRetries.toDouble()
}
