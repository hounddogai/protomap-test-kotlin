package com.acme.transport.customs.clearance.target

/** How screens and exports show a pending clearance review. */
data class PendingClearanceReviewView(
    var code: String,
    var titleText: String,
    var stopCount: String,
    var sequence: String,
    var reviewerText: String,
    var amount: String,
    var summary: String,
    var margin: String,
    var fuelLiters: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toPendingClearanceReviewView(item: PendingClearanceReview): PendingClearanceReviewView {
    return PendingClearanceReviewView(
        code = item.code,
        titleText = item.title,
        stopCount = item.stopCount.toString(),
        sequence = item.sequence.toString(),
        reviewerText = item.reviewer,
        amount = "%.2f".format(item.amount),
        summary = item.summary,
        margin = "%.2f".format(item.margin),
        fuelLiters = "%.2f".format(item.fuelLiters),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingClearanceReviewViews(items: MutableList<PendingClearanceReview>): MutableList<PendingClearanceReviewView> {
    val views = mutableListOf<PendingClearanceReviewView>()
    for (item in items) {
        views.add(toPendingClearanceReviewView(item))
    }
    return views
}

fun sumPendingClearanceReviewView(view: PendingClearanceReviewView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedStopCount.toDouble() + parsedSequence.toDouble() + parsedAmount
}
