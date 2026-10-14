package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default scorecard allocation. */
data class DefaultScorecardAllocationView(
    var code: String,
    var title: String,
    var phoneText: String,
    var capacityText: String,
    var depot: String,
    var currency: String,
    var ownerId: String,
    var floorValueText: String,
    var quantity: String,
    var visible: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toDefaultScorecardAllocationView(item: DefaultScorecardAllocation): DefaultScorecardAllocationView {
    return DefaultScorecardAllocationView(
        code = item.code,
        title = truncate(item.title, 64),
        phoneText = truncate(item.phone, 80),
        capacityText = item.capacity.toString(),
        depot = item.depot,
        currency = item.currency,
        ownerId = item.ownerId,
        floorValueText = "%.2f".format(item.floorValue),
        quantity = item.quantity.toString(),
        visible = "${item.visible}",
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultScorecardAllocationViews(items: MutableList<DefaultScorecardAllocation>): MutableList<DefaultScorecardAllocationView> {
    val views = mutableListOf<DefaultScorecardAllocationView>()
    for (item in items) {
        views.add(toDefaultScorecardAllocationView(item))
    }
    return views
}

fun sumDefaultScorecardAllocationView(view: DefaultScorecardAllocationView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedCapacity.toDouble() + parsedFloorValue + parsedQuantity.toDouble()
}
