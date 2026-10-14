package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card window. */
data class RateCardWindowView(
    var code: String,
    var name: String,
    var phoneText: String,
    var label: String,
    var category: String,
    var lane: String,
    var capacityText: String,
    var revision: String,
    var priority: String,
    var timezone: String,
    var statusLabel: String,
)

fun toRateCardWindowView(item: RateCardWindow): RateCardWindowView {
    return RateCardWindowView(
        code = item.code,
        name = item.name,
        phoneText = item.phone,
        label = truncate(item.label, 40),
        category = item.category,
        lane = truncate(item.lane, 40),
        capacityText = item.capacity.toString(),
        revision = item.revision.toString(),
        priority = item.priority.toString(),
        timezone = truncate(item.timezone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardWindowViews(items: MutableList<RateCardWindow>): MutableList<RateCardWindowView> {
    val views = mutableListOf<RateCardWindowView>()
    for (item in items) {
        views.add(toRateCardWindowView(item))
    }
    return views
}

fun sumRateCardWindowView(view: RateCardWindowView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedCapacity.toDouble() + parsedRevision.toDouble() + parsedPriority.toDouble()
}
