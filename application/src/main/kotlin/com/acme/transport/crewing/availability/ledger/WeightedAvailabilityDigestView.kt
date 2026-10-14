package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted availability digest. */
data class WeightedAvailabilityDigestView(
    var codeText: String,
    var title: String,
    var depot: String,
    var graceDaysText: String,
    var maximum: String,
    var amount: String,
    var locale: String,
    var startsAt: String,
    var minimum: String,
    var quantityText: String,
    var weight: String,
    var statusLabel: String,
)

fun toWeightedAvailabilityDigestView(item: WeightedAvailabilityDigest): WeightedAvailabilityDigestView {
    return WeightedAvailabilityDigestView(
        codeText = item.code,
        title = truncate(item.title, 64),
        depot = item.depot,
        graceDaysText = item.graceDays.toString(),
        maximum = "%.2f".format(item.maximum),
        amount = "%.2f".format(item.amount),
        locale = item.locale,
        startsAt = item.startsAt.toString(),
        minimum = "%.2f".format(item.minimum),
        quantityText = item.quantity.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAvailabilityDigestViews(items: MutableList<WeightedAvailabilityDigest>): MutableList<WeightedAvailabilityDigestView> {
    val views = mutableListOf<WeightedAvailabilityDigestView>()
    for (item in items) {
        views.add(toWeightedAvailabilityDigestView(item))
    }
    return views
}

fun sumWeightedAvailabilityDigestView(view: WeightedAvailabilityDigestView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedGraceDays.toDouble() + parsedMaximum + parsedAmount
}
