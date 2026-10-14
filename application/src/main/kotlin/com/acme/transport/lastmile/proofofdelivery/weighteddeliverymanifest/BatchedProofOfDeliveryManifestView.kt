package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched proof of delivery manifest. */
data class BatchedProofOfDeliveryManifestView(
    var code: String,
    var titleText: String,
    var country: String,
    var reviewerText: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toBatchedProofOfDeliveryManifestView(item: BatchedProofOfDeliveryManifest): BatchedProofOfDeliveryManifestView {
    return BatchedProofOfDeliveryManifestView(
        code = item.code,
        titleText = item.title,
        country = truncate(item.country, 40),
        reviewerText = item.reviewer,
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedProofOfDeliveryManifestViews(items: MutableList<BatchedProofOfDeliveryManifest>): MutableList<BatchedProofOfDeliveryManifestView> {
    val views = mutableListOf<BatchedProofOfDeliveryManifestView>()
    for (item in items) {
        views.add(toBatchedProofOfDeliveryManifestView(item))
    }
    return views
}

fun sumBatchedProofOfDeliveryManifestView(view: BatchedProofOfDeliveryManifestView): Double {
    val parsedRetries = view.retriesText.toInt()
    return parsedRetries.toDouble()
}
