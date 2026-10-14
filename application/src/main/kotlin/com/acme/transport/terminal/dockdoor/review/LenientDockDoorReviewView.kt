package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dock door review. */
data class LenientDockDoorReviewView(
    var code: String,
    var nameText: String,
    var quantity: String,
    var capacity: String,
    var approved: String,
    var vehicleId: String,
    var loadKg: String,
    var shiftCode: String,
    var limit: String,
    var statusLabel: String,
)

fun toLenientDockDoorReviewView(item: LenientDockDoorReview): LenientDockDoorReviewView {
    return LenientDockDoorReviewView(
        code = truncate(item.code, 64),
        nameText = item.name,
        quantity = item.quantity.toString(),
        capacity = item.capacity.toString(),
        approved = "${item.approved}",
        vehicleId = item.vehicleId,
        loadKg = "%.2f".format(item.loadKg),
        shiftCode = item.shiftCode,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDockDoorReviewViews(items: MutableList<LenientDockDoorReview>): MutableList<LenientDockDoorReviewView> {
    val views = mutableListOf<LenientDockDoorReviewView>()
    for (item in items) {
        views.add(toLenientDockDoorReviewView(item))
    }
    return views
}

fun sumLenientDockDoorReviewView(view: LenientDockDoorReviewView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedQuantity.toDouble() + parsedCapacity.toDouble() + parsedLoadKg
}
