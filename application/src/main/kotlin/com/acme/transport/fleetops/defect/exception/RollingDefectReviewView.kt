package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling defect review. */
data class RollingDefectReviewView(
    var code: String,
    var title: String,
    var weight: String,
    var externalRefText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toRollingDefectReviewView(item: RollingDefectReview): RollingDefectReviewView {
    return RollingDefectReviewView(
        code = item.code,
        title = item.title,
        weight = "%.2f".format(item.weight),
        externalRefText = item.externalRef,
        ownerId = truncate(item.ownerId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDefectReviewViews(items: MutableList<RollingDefectReview>): MutableList<RollingDefectReviewView> {
    val views = mutableListOf<RollingDefectReviewView>()
    for (item in items) {
        views.add(toRollingDefectReviewView(item))
    }
    return views
}

fun sumRollingDefectReviewView(view: RollingDefectReviewView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
