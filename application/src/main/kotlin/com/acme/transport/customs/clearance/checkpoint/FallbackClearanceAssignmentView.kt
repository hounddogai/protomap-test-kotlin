package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback clearance assignment. */
data class FallbackClearanceAssignmentView(
    var code: String,
    var labelText: String,
    var discountText: String,
    var etaMinutesText: String,
    var notes: String,
    var limit: String,
    var timezone: String,
    var thresholdText: String,
    var vehicleId: String,
    var endsAtText: String,
    var costText: String,
    var statusLabel: String,
)

fun toFallbackClearanceAssignmentView(item: FallbackClearanceAssignment): FallbackClearanceAssignmentView {
    return FallbackClearanceAssignmentView(
        code = truncate(item.code, 80),
        labelText = item.label,
        discountText = "%.2f".format(item.discount),
        etaMinutesText = item.etaMinutes.toString(),
        notes = truncate(item.notes, 80),
        limit = item.limit.toString(),
        timezone = item.timezone,
        thresholdText = "%.2f".format(item.threshold),
        vehicleId = truncate(item.vehicleId, 120),
        endsAtText = item.endsAt.toString(),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackClearanceAssignmentViews(items: MutableList<FallbackClearanceAssignment>): MutableList<FallbackClearanceAssignmentView> {
    val views = mutableListOf<FallbackClearanceAssignmentView>()
    for (item in items) {
        views.add(toFallbackClearanceAssignmentView(item))
    }
    return views
}

fun sumFallbackClearanceAssignmentView(view: FallbackClearanceAssignmentView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedDiscount + parsedEtaMinutes.toDouble() + parsedLimit.toDouble()
}
