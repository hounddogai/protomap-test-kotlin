package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay profile. */
data class RelayProfileView(
    var code: String,
    var title: String,
    var floorValueText: String,
    var thresholdText: String,
    var priority: String,
    var limit: String,
    var approvedText: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toRelayProfileView(item: RelayProfile): RelayProfileView {
    return RelayProfileView(
        code = item.code,
        title = truncate(item.title, 80),
        floorValueText = "%.2f".format(item.floorValue),
        thresholdText = "%.2f".format(item.threshold),
        priority = item.priority.toString(),
        limit = item.limit.toString(),
        approvedText = "${item.approved}",
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayProfileViews(items: MutableList<RelayProfile>): MutableList<RelayProfileView> {
    val views = mutableListOf<RelayProfileView>()
    for (item in items) {
        views.add(toRelayProfileView(item))
    }
    return views
}

fun sumRelayProfileView(view: RelayProfileView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedFloorValue + parsedThreshold + parsedPriority.toDouble()
}
