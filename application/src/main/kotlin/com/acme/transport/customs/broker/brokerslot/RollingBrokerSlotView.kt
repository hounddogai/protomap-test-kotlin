package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling broker slot. */
data class RollingBrokerSlotView(
    var code: String,
    var nameText: String,
    var trailerId: String,
    var depot: String,
    var createdAt: String,
    var expiresAtText: String,
    var limitText: String,
    var threshold: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toRollingBrokerSlotView(item: RollingBrokerSlot): RollingBrokerSlotView {
    return RollingBrokerSlotView(
        code = item.code,
        nameText = item.name,
        trailerId = item.trailerId,
        depot = truncate(item.depot, 40),
        createdAt = item.createdAt.toString(),
        expiresAtText = item.expiresAt.toString(),
        limitText = item.limit.toString(),
        threshold = "%.2f".format(item.threshold),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingBrokerSlotViews(items: MutableList<RollingBrokerSlot>): MutableList<RollingBrokerSlotView> {
    val views = mutableListOf<RollingBrokerSlotView>()
    for (item in items) {
        views.add(toRollingBrokerSlotView(item))
    }
    return views
}

fun sumRollingBrokerSlotView(view: RollingBrokerSlotView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedCreatedAt.toDouble() + parsedExpiresAt.toDouble() + parsedLimit.toDouble()
}
