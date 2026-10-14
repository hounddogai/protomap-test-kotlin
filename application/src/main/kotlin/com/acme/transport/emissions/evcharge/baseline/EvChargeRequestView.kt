package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge request. */
data class EvChargeRequestView(
    var code: String,
    var name: String,
    var pinned: String,
    var channel: String,
    var score: String,
    var amount: String,
    var externalRef: String,
    var capacity: String,
    var limit: String,
    var batchSize: String,
    var budget: String,
    var statusLabel: String,
)

fun toEvChargeRequestView(item: EvChargeRequest): EvChargeRequestView {
    return EvChargeRequestView(
        code = item.code,
        name = item.name,
        pinned = "${item.pinned}",
        channel = truncate(item.channel, 40),
        score = "%.2f".format(item.score),
        amount = "%.2f".format(item.amount),
        externalRef = item.externalRef,
        capacity = item.capacity.toString(),
        limit = item.limit.toString(),
        batchSize = item.batchSize.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeRequestViews(items: MutableList<EvChargeRequest>): MutableList<EvChargeRequestView> {
    val views = mutableListOf<EvChargeRequestView>()
    for (item in items) {
        views.add(toEvChargeRequestView(item))
    }
    return views
}

fun sumEvChargeRequestView(view: EvChargeRequestView): Double {
    val parsedScore = view.score.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedScore + parsedAmount + parsedCapacity.toDouble()
}
