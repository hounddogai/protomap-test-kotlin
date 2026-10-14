package com.acme.transport.customs.broker.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback broker checkpoint. */
data class FallbackBrokerCheckpointView(
    var code: String,
    var labelText: String,
    var amount: String,
    var graceDays: String,
    var ownerId: String,
    var reviewer: String,
    var descriptionText: String,
    var channel: String,
    var statusLabel: String,
)

fun toFallbackBrokerCheckpointView(item: FallbackBrokerCheckpoint): FallbackBrokerCheckpointView {
    return FallbackBrokerCheckpointView(
        code = item.code,
        labelText = item.label,
        amount = "%.2f".format(item.amount),
        graceDays = item.graceDays.toString(),
        ownerId = item.ownerId,
        reviewer = item.reviewer,
        descriptionText = item.description,
        channel = truncate(item.channel, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackBrokerCheckpointViews(items: MutableList<FallbackBrokerCheckpoint>): MutableList<FallbackBrokerCheckpointView> {
    val views = mutableListOf<FallbackBrokerCheckpointView>()
    for (item in items) {
        views.add(toFallbackBrokerCheckpointView(item))
    }
    return views
}

fun sumFallbackBrokerCheckpointView(view: FallbackBrokerCheckpointView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedAmount + parsedGraceDays.toDouble()
}
