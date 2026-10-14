package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback inspection allocation. */
data class FallbackInspectionAllocationView(
    var code: String,
    var name: String,
    var shiftCode: String,
    var maximumText: String,
    var limit: String,
    var statusLabel: String,
)

fun toFallbackInspectionAllocationView(item: FallbackInspectionAllocation): FallbackInspectionAllocationView {
    return FallbackInspectionAllocationView(
        code = item.code,
        name = item.name,
        shiftCode = truncate(item.shiftCode, 40),
        maximumText = "%.2f".format(item.maximum),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackInspectionAllocationViews(items: MutableList<FallbackInspectionAllocation>): MutableList<FallbackInspectionAllocationView> {
    val views = mutableListOf<FallbackInspectionAllocationView>()
    for (item in items) {
        views.add(toFallbackInspectionAllocationView(item))
    }
    return views
}

fun sumFallbackInspectionAllocationView(view: FallbackInspectionAllocationView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedMaximum + parsedLimit.toDouble()
}
