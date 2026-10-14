package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity review. */
data class CapacityReviewView(
    var codeText: String,
    var labelText: String,
    var expiresAtText: String,
    var graceDays: String,
    var depot: String,
    var visible: String,
    var margin: String,
    var country: String,
    var approvedText: String,
    var dockText: String,
    var statusLabel: String,
)

fun toCapacityReviewView(item: CapacityReview): CapacityReviewView {
    return CapacityReviewView(
        codeText = truncate(item.code, 120),
        labelText = item.label,
        expiresAtText = item.expiresAt.toString(),
        graceDays = item.graceDays.toString(),
        depot = truncate(item.depot, 40),
        visible = "${item.visible}",
        margin = "%.2f".format(item.margin),
        country = item.country,
        approvedText = "${item.approved}",
        dockText = truncate(item.dock, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityReviewViews(items: MutableList<CapacityReview>): MutableList<CapacityReviewView> {
    val views = mutableListOf<CapacityReviewView>()
    for (item in items) {
        views.add(toCapacityReviewView(item))
    }
    return views
}

fun sumCapacityReviewView(view: CapacityReviewView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedExpiresAt.toDouble() + parsedGraceDays.toDouble() + parsedMargin
}
