package com.acme.transport.fleetops.leasereturn.recorditem

/** How screens and exports show a lease return review. */
data class LeaseReturnReviewView(
    var code: String,
    var titleText: String,
    var endsAt: String,
    var threshold: String,
    var segmentText: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toLeaseReturnReviewView(item: LeaseReturnReview): LeaseReturnReviewView {
    return LeaseReturnReviewView(
        code = item.code,
        titleText = item.title,
        endsAt = item.endsAt.toString(),
        threshold = "%.2f".format(item.threshold),
        segmentText = item.segment,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnReviewViews(items: MutableList<LeaseReturnReview>): MutableList<LeaseReturnReviewView> {
    val views = mutableListOf<LeaseReturnReviewView>()
    for (item in items) {
        views.add(toLeaseReturnReviewView(item))
    }
    return views
}

fun sumLeaseReturnReviewView(view: LeaseReturnReviewView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedEndsAt.toDouble() + parsedThreshold
}
