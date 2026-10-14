package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched near miss plan. */
data class BatchedNearMissPlanView(
    var code: String,
    var nameText: String,
    var country: String,
    var vehicleId: String,
    var region: String,
    var visible: String,
    var approvedText: String,
    var amount: String,
    var statusLabel: String,
)

fun toBatchedNearMissPlanView(item: BatchedNearMissPlan): BatchedNearMissPlanView {
    return BatchedNearMissPlanView(
        code = truncate(item.code, 40),
        nameText = item.name,
        country = item.country,
        vehicleId = item.vehicleId,
        region = item.region,
        visible = "${item.visible}",
        approvedText = "${item.approved}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedNearMissPlanViews(items: MutableList<BatchedNearMissPlan>): MutableList<BatchedNearMissPlanView> {
    val views = mutableListOf<BatchedNearMissPlanView>()
    for (item in items) {
        views.add(toBatchedNearMissPlanView(item))
    }
    return views
}

fun sumBatchedNearMissPlanView(view: BatchedNearMissPlanView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
