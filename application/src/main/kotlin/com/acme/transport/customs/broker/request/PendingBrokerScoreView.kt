package com.acme.transport.customs.broker.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending broker score. */
data class PendingBrokerScoreView(
    var code: String,
    var name: String,
    var externalRefText: String,
    var cost: String,
    var attempts: String,
    var threshold: String,
    var weight: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toPendingBrokerScoreView(item: PendingBrokerScore): PendingBrokerScoreView {
    return PendingBrokerScoreView(
        code = truncate(item.code, 40),
        name = item.name,
        externalRefText = truncate(item.externalRef, 64),
        cost = "%.2f".format(item.cost),
        attempts = item.attempts.toString(),
        threshold = "%.2f".format(item.threshold),
        weight = "%.2f".format(item.weight),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingBrokerScoreViews(items: MutableList<PendingBrokerScore>): MutableList<PendingBrokerScoreView> {
    val views = mutableListOf<PendingBrokerScoreView>()
    for (item in items) {
        views.add(toPendingBrokerScoreView(item))
    }
    return views
}

fun sumPendingBrokerScoreView(view: PendingBrokerScoreView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedCost + parsedAttempts.toDouble() + parsedThreshold
}
