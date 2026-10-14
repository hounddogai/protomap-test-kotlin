package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional proof of delivery review. */
data class RegionalProofOfDeliveryReviewView(
    var code: String,
    var name: String,
    var dock: String,
    var contactEmail: String,
    var batchSize: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toRegionalProofOfDeliveryReviewView(item: RegionalProofOfDeliveryReview): RegionalProofOfDeliveryReviewView {
    return RegionalProofOfDeliveryReviewView(
        code = truncate(item.code, 80),
        name = item.name,
        dock = truncate(item.dock, 40),
        contactEmail = truncate(item.contactEmail, 64),
        batchSize = item.batchSize.toString(),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalProofOfDeliveryReviewViews(items: MutableList<RegionalProofOfDeliveryReview>): MutableList<RegionalProofOfDeliveryReviewView> {
    val views = mutableListOf<RegionalProofOfDeliveryReviewView>()
    for (item in items) {
        views.add(toRegionalProofOfDeliveryReviewView(item))
    }
    return views
}

fun sumRegionalProofOfDeliveryReviewView(view: RegionalProofOfDeliveryReviewView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedBatchSize.toDouble() + parsedEndsAt.toDouble()
}
