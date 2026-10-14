package com.acme.transport.lastmile.proofofdelivery.cycle

/** How screens and exports show a batched proof of delivery review. */
data class BatchedProofOfDeliveryReviewView(
    var code: String,
    var name: String,
    var carrier: String,
    var score: String,
    var budget: String,
    var statusLabel: String,
)

fun toBatchedProofOfDeliveryReviewView(item: BatchedProofOfDeliveryReview): BatchedProofOfDeliveryReviewView {
    return BatchedProofOfDeliveryReviewView(
        code = item.code,
        name = item.name,
        carrier = item.carrier,
        score = "%.2f".format(item.score),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedProofOfDeliveryReviewViews(items: MutableList<BatchedProofOfDeliveryReview>): MutableList<BatchedProofOfDeliveryReviewView> {
    val views = mutableListOf<BatchedProofOfDeliveryReviewView>()
    for (item in items) {
        views.add(toBatchedProofOfDeliveryReviewView(item))
    }
    return views
}

fun sumBatchedProofOfDeliveryReviewView(view: BatchedProofOfDeliveryReviewView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedScore + parsedBudget
}
