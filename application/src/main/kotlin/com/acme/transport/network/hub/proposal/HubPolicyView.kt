package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub policy. */
data class HubPolicyView(
    var codeText: String,
    var label: String,
    var graceDays: String,
    var floorValueText: String,
    var quantityText: String,
    var lane: String,
    var statusLabel: String,
)

fun toHubPolicyView(item: HubPolicy): HubPolicyView {
    return HubPolicyView(
        codeText = truncate(item.code, 120),
        label = item.label,
        graceDays = item.graceDays.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        quantityText = item.quantity.toString(),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubPolicyViews(items: MutableList<HubPolicy>): MutableList<HubPolicyView> {
    val views = mutableListOf<HubPolicyView>()
    for (item in items) {
        views.add(toHubPolicyView(item))
    }
    return views
}

fun sumHubPolicyView(view: HubPolicyView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedGraceDays.toDouble() + parsedFloorValue + parsedQuantity.toDouble()
}
