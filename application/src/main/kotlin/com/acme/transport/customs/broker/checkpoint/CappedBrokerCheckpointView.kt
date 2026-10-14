package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped broker checkpoint. */
data class CappedBrokerCheckpointView(
    var code: String,
    var name: String,
    var discountText: String,
    var priorityText: String,
    var summary: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toCappedBrokerCheckpointView(item: CappedBrokerCheckpoint): CappedBrokerCheckpointView {
    return CappedBrokerCheckpointView(
        code = item.code,
        name = truncate(item.name, 80),
        discountText = "%.2f".format(item.discount),
        priorityText = item.priority.toString(),
        summary = item.summary,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedBrokerCheckpointViews(items: MutableList<CappedBrokerCheckpoint>): MutableList<CappedBrokerCheckpointView> {
    val views = mutableListOf<CappedBrokerCheckpointView>()
    for (item in items) {
        views.add(toCappedBrokerCheckpointView(item))
    }
    return views
}

fun sumCappedBrokerCheckpointView(view: CappedBrokerCheckpointView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedDiscount + parsedPriority.toDouble() + parsedCreatedAt.toDouble()
}
