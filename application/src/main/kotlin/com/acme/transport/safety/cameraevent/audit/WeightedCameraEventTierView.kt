package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted camera event tier. */
data class WeightedCameraEventTierView(
    var code: String,
    var nameText: String,
    var approved: String,
    var segment: String,
    var carrier: String,
    var pinnedText: String,
    var amount: String,
    var statusLabel: String,
)

fun toWeightedCameraEventTierView(item: WeightedCameraEventTier): WeightedCameraEventTierView {
    return WeightedCameraEventTierView(
        code = item.code,
        nameText = item.name,
        approved = "${item.approved}",
        segment = truncate(item.segment, 80),
        carrier = truncate(item.carrier, 120),
        pinnedText = "${item.pinned}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedCameraEventTierViews(items: MutableList<WeightedCameraEventTier>): MutableList<WeightedCameraEventTierView> {
    val views = mutableListOf<WeightedCameraEventTierView>()
    for (item in items) {
        views.add(toWeightedCameraEventTierView(item))
    }
    return views
}

fun sumWeightedCameraEventTierView(view: WeightedCameraEventTierView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
