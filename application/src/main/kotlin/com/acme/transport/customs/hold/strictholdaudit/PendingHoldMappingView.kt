package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending hold mapping. */
data class PendingHoldMappingView(
    var code: String,
    var label: String,
    var approved: String,
    var sequence: String,
    var durationMinutesText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toPendingHoldMappingView(item: PendingHoldMapping): PendingHoldMappingView {
    return PendingHoldMappingView(
        code = item.code,
        label = truncate(item.label, 120),
        approved = "${item.approved}",
        sequence = item.sequence.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingHoldMappingViews(items: MutableList<PendingHoldMapping>): MutableList<PendingHoldMappingView> {
    val views = mutableListOf<PendingHoldMappingView>()
    for (item in items) {
        views.add(toPendingHoldMappingView(item))
    }
    return views
}

fun sumPendingHoldMappingView(view: PendingHoldMappingView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedSequence.toDouble() + parsedDurationMinutes.toDouble() + parsedFuelLiters
}
