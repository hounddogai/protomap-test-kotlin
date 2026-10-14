package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback ev charge threshold. */
data class FallbackEvChargeThresholdView(
    var code: String,
    var title: String,
    var createdAt: String,
    var reviewerText: String,
    var activeText: String,
    var notes: String,
    var categoryText: String,
    var amount: String,
    var statusLabel: String,
)

fun toFallbackEvChargeThresholdView(item: FallbackEvChargeThreshold): FallbackEvChargeThresholdView {
    return FallbackEvChargeThresholdView(
        code = item.code,
        title = truncate(item.title, 120),
        createdAt = item.createdAt.toString(),
        reviewerText = item.reviewer,
        activeText = "${item.active}",
        notes = item.notes,
        categoryText = truncate(item.category, 64),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEvChargeThresholdViews(items: MutableList<FallbackEvChargeThreshold>): MutableList<FallbackEvChargeThresholdView> {
    val views = mutableListOf<FallbackEvChargeThresholdView>()
    for (item in items) {
        views.add(toFallbackEvChargeThresholdView(item))
    }
    return views
}

fun sumFallbackEvChargeThresholdView(view: FallbackEvChargeThresholdView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedCreatedAt.toDouble() + parsedAmount
}
