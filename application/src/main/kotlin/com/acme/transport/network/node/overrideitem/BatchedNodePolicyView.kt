package com.acme.transport.network.node.overrideitem

/** How screens and exports show a batched node policy. */
data class BatchedNodePolicyView(
    var code: String,
    var label: String,
    var scoreText: String,
    var quantityText: String,
    var weight: String,
    var approved: String,
    var statusLabel: String,
)

fun toBatchedNodePolicyView(item: BatchedNodePolicy): BatchedNodePolicyView {
    return BatchedNodePolicyView(
        code = item.code,
        label = item.label,
        scoreText = "%.2f".format(item.score),
        quantityText = item.quantity.toString(),
        weight = "%.2f".format(item.weight),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedNodePolicyViews(items: MutableList<BatchedNodePolicy>): MutableList<BatchedNodePolicyView> {
    val views = mutableListOf<BatchedNodePolicyView>()
    for (item in items) {
        views.add(toBatchedNodePolicyView(item))
    }
    return views
}

fun sumBatchedNodePolicyView(view: BatchedNodePolicyView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedScore + parsedQuantity.toDouble() + parsedWeight
}
