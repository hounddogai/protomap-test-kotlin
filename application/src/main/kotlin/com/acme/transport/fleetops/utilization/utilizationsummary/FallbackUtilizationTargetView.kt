package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback utilization target. */
data class FallbackUtilizationTargetView(
    var code: String,
    var label: String,
    var timezone: String,
    var notes: String,
    var quantity: String,
    var statusLabel: String,
)

fun toFallbackUtilizationTargetView(item: FallbackUtilizationTarget): FallbackUtilizationTargetView {
    return FallbackUtilizationTargetView(
        code = item.code,
        label = item.label,
        timezone = truncate(item.timezone, 80),
        notes = truncate(item.notes, 80),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackUtilizationTargetViews(items: MutableList<FallbackUtilizationTarget>): MutableList<FallbackUtilizationTargetView> {
    val views = mutableListOf<FallbackUtilizationTargetView>()
    for (item in items) {
        views.add(toFallbackUtilizationTargetView(item))
    }
    return views
}

fun sumFallbackUtilizationTargetView(view: FallbackUtilizationTargetView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
