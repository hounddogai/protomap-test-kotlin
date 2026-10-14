package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched service plan group. */
data class BatchedServicePlanGroupView(
    var code: String,
    var name: String,
    var sourceText: String,
    var rate: String,
    var pallets: String,
    var statusLabel: String,
)

fun toBatchedServicePlanGroupView(item: BatchedServicePlanGroup): BatchedServicePlanGroupView {
    return BatchedServicePlanGroupView(
        code = item.code,
        name = truncate(item.name, 64),
        sourceText = item.source,
        rate = "%.2f".format(item.rate),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedServicePlanGroupViews(items: MutableList<BatchedServicePlanGroup>): MutableList<BatchedServicePlanGroupView> {
    val views = mutableListOf<BatchedServicePlanGroupView>()
    for (item in items) {
        views.add(toBatchedServicePlanGroupView(item))
    }
    return views
}

fun sumBatchedServicePlanGroupView(view: BatchedServicePlanGroupView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedRate + parsedPallets.toDouble()
}
