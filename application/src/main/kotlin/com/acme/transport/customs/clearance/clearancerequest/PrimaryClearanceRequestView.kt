package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary clearance request. */
data class PrimaryClearanceRequestView(
    var codeText: String,
    var labelText: String,
    var shiftCode: String,
    var discount: String,
    var distanceKm: String,
    var costText: String,
    var approvedText: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toPrimaryClearanceRequestView(item: PrimaryClearanceRequest): PrimaryClearanceRequestView {
    return PrimaryClearanceRequestView(
        codeText = truncate(item.code, 64),
        labelText = item.label,
        shiftCode = item.shiftCode,
        discount = "%.2f".format(item.discount),
        distanceKm = "%.2f".format(item.distanceKm),
        costText = "%.2f".format(item.cost),
        approvedText = "${item.approved}",
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryClearanceRequestViews(items: MutableList<PrimaryClearanceRequest>): MutableList<PrimaryClearanceRequestView> {
    val views = mutableListOf<PrimaryClearanceRequestView>()
    for (item in items) {
        views.add(toPrimaryClearanceRequestView(item))
    }
    return views
}

fun sumPrimaryClearanceRequestView(view: PrimaryClearanceRequestView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedDiscount + parsedDistanceKm + parsedCost
}
