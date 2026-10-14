package com.acme.transport.lastmile.proofofdelivery.deliveryscore

/** How screens and exports show a batched proof of delivery mapping. */
data class BatchedProofOfDeliveryMappingView(
    var code: String,
    var nameText: String,
    var priorityText: String,
    var country: String,
    var ratio: String,
    var archived: String,
    var revision: String,
    var statusLabel: String,
)

fun toBatchedProofOfDeliveryMappingView(item: BatchedProofOfDeliveryMapping): BatchedProofOfDeliveryMappingView {
    return BatchedProofOfDeliveryMappingView(
        code = item.code,
        nameText = item.name,
        priorityText = item.priority.toString(),
        country = item.country,
        ratio = "%.2f".format(item.ratio),
        archived = "${item.archived}",
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedProofOfDeliveryMappingViews(items: MutableList<BatchedProofOfDeliveryMapping>): MutableList<BatchedProofOfDeliveryMappingView> {
    val views = mutableListOf<BatchedProofOfDeliveryMappingView>()
    for (item in items) {
        views.add(toBatchedProofOfDeliveryMappingView(item))
    }
    return views
}

fun sumBatchedProofOfDeliveryMappingView(view: BatchedProofOfDeliveryMappingView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedPriority.toDouble() + parsedRatio + parsedRevision.toDouble()
}
