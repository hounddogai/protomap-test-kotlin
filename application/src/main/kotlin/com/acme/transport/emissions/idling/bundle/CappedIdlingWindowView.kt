package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped idling window. */
data class CappedIdlingWindowView(
    var code: String,
    var title: String,
    var lane: String,
    var amount: String,
    var minimum: String,
    var capacityText: String,
    var carrier: String,
    var priority: String,
    var expiresAtText: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toCappedIdlingWindowView(item: CappedIdlingWindow): CappedIdlingWindowView {
    return CappedIdlingWindowView(
        code = item.code,
        title = item.title,
        lane = truncate(item.lane, 80),
        amount = "%.2f".format(item.amount),
        minimum = "%.2f".format(item.minimum),
        capacityText = item.capacity.toString(),
        carrier = item.carrier,
        priority = item.priority.toString(),
        expiresAtText = item.expiresAt.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedIdlingWindowViews(items: MutableList<CappedIdlingWindow>): MutableList<CappedIdlingWindowView> {
    val views = mutableListOf<CappedIdlingWindowView>()
    for (item in items) {
        views.add(toCappedIdlingWindowView(item))
    }
    return views
}

fun sumCappedIdlingWindowView(view: CappedIdlingWindowView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedAmount + parsedMinimum + parsedCapacity.toDouble()
}
