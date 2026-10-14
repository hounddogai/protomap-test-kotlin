package com.acme.transport.fleetops.leasereturn.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched lease return variance. */
data class BatchedLeaseReturnVarianceView(
    var code: String,
    var label: String,
    var pinned: String,
    var region: String,
    var localeText: String,
    var updatedAt: String,
    var budgetText: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toBatchedLeaseReturnVarianceView(item: BatchedLeaseReturnVariance): BatchedLeaseReturnVarianceView {
    return BatchedLeaseReturnVarianceView(
        code = truncate(item.code, 120),
        label = item.label,
        pinned = "${item.pinned}",
        region = item.region,
        localeText = truncate(item.locale, 120),
        updatedAt = item.updatedAt.toString(),
        budgetText = "%.2f".format(item.budget),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedLeaseReturnVarianceViews(items: MutableList<BatchedLeaseReturnVariance>): MutableList<BatchedLeaseReturnVarianceView> {
    val views = mutableListOf<BatchedLeaseReturnVarianceView>()
    for (item in items) {
        views.add(toBatchedLeaseReturnVarianceView(item))
    }
    return views
}

fun sumBatchedLeaseReturnVarianceView(view: BatchedLeaseReturnVarianceView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedUpdatedAt.toDouble() + parsedBudget + parsedGraceDays.toDouble()
}
