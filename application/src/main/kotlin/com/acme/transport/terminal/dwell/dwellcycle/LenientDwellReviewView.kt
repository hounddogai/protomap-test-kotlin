package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dwell review. */
data class LenientDwellReviewView(
    var code: String,
    var label: String,
    var pallets: String,
    var thresholdText: String,
    var minimum: String,
    var reviewerText: String,
    var depot: String,
    var fuelLitersText: String,
    var active: String,
    var visible: String,
    var statusLabel: String,
)

fun toLenientDwellReviewView(item: LenientDwellReview): LenientDwellReviewView {
    return LenientDwellReviewView(
        code = truncate(item.code, 40),
        label = item.label,
        pallets = item.pallets.toString(),
        thresholdText = "%.2f".format(item.threshold),
        minimum = "%.2f".format(item.minimum),
        reviewerText = truncate(item.reviewer, 64),
        depot = item.depot,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        active = "${item.active}",
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDwellReviewViews(items: MutableList<LenientDwellReview>): MutableList<LenientDwellReviewView> {
    val views = mutableListOf<LenientDwellReviewView>()
    for (item in items) {
        views.add(toLenientDwellReviewView(item))
    }
    return views
}

fun sumLenientDwellReviewView(view: LenientDwellReviewView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedPallets.toDouble() + parsedThreshold + parsedMinimum
}
