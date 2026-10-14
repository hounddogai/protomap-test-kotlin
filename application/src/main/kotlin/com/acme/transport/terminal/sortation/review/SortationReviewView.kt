package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation review. */
data class SortationReviewView(
    var code: String,
    var name: String,
    var maximumText: String,
    var driverId: String,
    var region: String,
    var notes: String,
    var activeText: String,
    var cost: String,
    var statusLabel: String,
)

fun toSortationReviewView(item: SortationReview): SortationReviewView {
    return SortationReviewView(
        code = truncate(item.code, 40),
        name = item.name,
        maximumText = "%.2f".format(item.maximum),
        driverId = item.driverId,
        region = item.region,
        notes = item.notes,
        activeText = "${item.active}",
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationReviewViews(items: MutableList<SortationReview>): MutableList<SortationReviewView> {
    val views = mutableListOf<SortationReviewView>()
    for (item in items) {
        views.add(toSortationReviewView(item))
    }
    return views
}

fun sumSortationReviewView(view: SortationReviewView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedMaximum + parsedCost
}
