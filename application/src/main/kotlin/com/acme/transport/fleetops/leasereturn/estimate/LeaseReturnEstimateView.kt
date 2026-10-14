package com.acme.transport.fleetops.leasereturn.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return estimate. */
data class LeaseReturnEstimateView(
    var code: String,
    var title: String,
    var floorValueText: String,
    var category: String,
    var quantityText: String,
    var margin: String,
    var source: String,
    var visible: String,
    var statusLabel: String,
)

fun toLeaseReturnEstimateView(item: LeaseReturnEstimate): LeaseReturnEstimateView {
    return LeaseReturnEstimateView(
        code = item.code,
        title = item.title,
        floorValueText = "%.2f".format(item.floorValue),
        category = item.category,
        quantityText = item.quantity.toString(),
        margin = "%.2f".format(item.margin),
        source = truncate(item.source, 120),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnEstimateViews(items: MutableList<LeaseReturnEstimate>): MutableList<LeaseReturnEstimateView> {
    val views = mutableListOf<LeaseReturnEstimateView>()
    for (item in items) {
        views.add(toLeaseReturnEstimateView(item))
    }
    return views
}

fun sumLeaseReturnEstimateView(view: LeaseReturnEstimateView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedFloorValue + parsedQuantity.toDouble() + parsedMargin
}
