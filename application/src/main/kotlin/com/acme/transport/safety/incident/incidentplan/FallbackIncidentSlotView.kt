package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback incident slot. */
data class FallbackIncidentSlotView(
    var code: String,
    var title: String,
    var depotText: String,
    var odometer: String,
    var rateText: String,
    var endsAt: String,
    var etaMinutes: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toFallbackIncidentSlotView(item: FallbackIncidentSlot): FallbackIncidentSlotView {
    return FallbackIncidentSlotView(
        code = item.code,
        title = truncate(item.title, 80),
        depotText = truncate(item.depot, 80),
        odometer = item.odometer.toString(),
        rateText = "%.2f".format(item.rate),
        endsAt = item.endsAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackIncidentSlotViews(items: MutableList<FallbackIncidentSlot>): MutableList<FallbackIncidentSlotView> {
    val views = mutableListOf<FallbackIncidentSlotView>()
    for (item in items) {
        views.add(toFallbackIncidentSlotView(item))
    }
    return views
}

fun sumFallbackIncidentSlotView(view: FallbackIncidentSlotView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRate = view.rateText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedOdometer.toDouble() + parsedRate + parsedEndsAt.toDouble()
}
