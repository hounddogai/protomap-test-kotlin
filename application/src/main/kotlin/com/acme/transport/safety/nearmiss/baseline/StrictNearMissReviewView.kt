package com.acme.transport.safety.nearmiss.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict near miss review. */
data class StrictNearMissReviewView(
    var code: String,
    var label: String,
    var externalRefText: String,
    var pallets: String,
    var margin: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toStrictNearMissReviewView(item: StrictNearMissReview): StrictNearMissReviewView {
    return StrictNearMissReviewView(
        code = truncate(item.code, 120),
        label = item.label,
        externalRefText = item.externalRef,
        pallets = item.pallets.toString(),
        margin = "%.2f".format(item.margin),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictNearMissReviewViews(items: MutableList<StrictNearMissReview>): MutableList<StrictNearMissReviewView> {
    val views = mutableListOf<StrictNearMissReviewView>()
    for (item in items) {
        views.add(toStrictNearMissReviewView(item))
    }
    return views
}

fun sumStrictNearMissReviewView(view: StrictNearMissReviewView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedPallets.toDouble() + parsedMargin + parsedFloorValue
}
