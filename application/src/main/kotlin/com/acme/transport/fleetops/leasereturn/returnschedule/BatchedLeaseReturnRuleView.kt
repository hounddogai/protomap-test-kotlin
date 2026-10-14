package com.acme.transport.fleetops.leasereturn.returnschedule

/** How screens and exports show a batched lease return rule. */
data class BatchedLeaseReturnRuleView(
    var code: String,
    var titleText: String,
    var name: String,
    var ratio: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toBatchedLeaseReturnRuleView(item: BatchedLeaseReturnRule): BatchedLeaseReturnRuleView {
    return BatchedLeaseReturnRuleView(
        code = item.code,
        titleText = item.title,
        name = item.name,
        ratio = "%.2f".format(item.ratio),
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedLeaseReturnRuleViews(items: MutableList<BatchedLeaseReturnRule>): MutableList<BatchedLeaseReturnRuleView> {
    val views = mutableListOf<BatchedLeaseReturnRuleView>()
    for (item in items) {
        views.add(toBatchedLeaseReturnRuleView(item))
    }
    return views
}

fun sumBatchedLeaseReturnRuleView(view: BatchedLeaseReturnRuleView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedRatio + parsedRevision.toDouble()
}
