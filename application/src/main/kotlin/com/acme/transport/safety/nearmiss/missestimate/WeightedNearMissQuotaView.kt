package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted near miss quota. */
data class WeightedNearMissQuotaView(
    var code: String,
    var nameText: String,
    var priority: String,
    var weightText: String,
    var amount: String,
    var carrier: String,
    var timezoneText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toWeightedNearMissQuotaView(item: WeightedNearMissQuota): WeightedNearMissQuotaView {
    return WeightedNearMissQuotaView(
        code = item.code,
        nameText = item.name,
        priority = item.priority.toString(),
        weightText = "%.2f".format(item.weight),
        amount = "%.2f".format(item.amount),
        carrier = truncate(item.carrier, 80),
        timezoneText = item.timezone,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedNearMissQuotaViews(items: MutableList<WeightedNearMissQuota>): MutableList<WeightedNearMissQuotaView> {
    val views = mutableListOf<WeightedNearMissQuotaView>()
    for (item in items) {
        views.add(toWeightedNearMissQuotaView(item))
    }
    return views
}

fun sumWeightedNearMissQuotaView(view: WeightedNearMissQuotaView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedPriority.toDouble() + parsedWeight + parsedAmount
}
