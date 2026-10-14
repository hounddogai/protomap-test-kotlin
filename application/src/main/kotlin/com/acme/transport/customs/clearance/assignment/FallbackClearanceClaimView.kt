package com.acme.transport.customs.clearance.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback clearance claim. */
data class FallbackClearanceClaimView(
    var code: String,
    var label: String,
    var stopCountText: String,
    var capacity: String,
    var etaMinutes: String,
    var ratioText: String,
    var shiftCode: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toFallbackClearanceClaimView(item: FallbackClearanceClaim): FallbackClearanceClaimView {
    return FallbackClearanceClaimView(
        code = truncate(item.code, 80),
        label = item.label,
        stopCountText = item.stopCount.toString(),
        capacity = item.capacity.toString(),
        etaMinutes = item.etaMinutes.toString(),
        ratioText = "%.2f".format(item.ratio),
        shiftCode = item.shiftCode,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackClearanceClaimViews(items: MutableList<FallbackClearanceClaim>): MutableList<FallbackClearanceClaimView> {
    val views = mutableListOf<FallbackClearanceClaimView>()
    for (item in items) {
        views.add(toFallbackClearanceClaimView(item))
    }
    return views
}

fun sumFallbackClearanceClaimView(view: FallbackClearanceClaimView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedStopCount.toDouble() + parsedCapacity.toDouble() + parsedEtaMinutes.toDouble()
}
