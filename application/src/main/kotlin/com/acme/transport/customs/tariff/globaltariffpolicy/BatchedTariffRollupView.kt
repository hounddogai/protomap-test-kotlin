package com.acme.transport.customs.tariff.globaltariffpolicy

/** How screens and exports show a batched tariff rollup. */
data class BatchedTariffRollupView(
    var code: String,
    var name: String,
    var regionText: String,
    var retriesText: String,
    var phone: String,
    var vehicleIdText: String,
    var visible: String,
    var cost: String,
    var statusLabel: String,
)

fun toBatchedTariffRollupView(item: BatchedTariffRollup): BatchedTariffRollupView {
    return BatchedTariffRollupView(
        code = item.code,
        name = item.name,
        regionText = item.region,
        retriesText = item.retries.toString(),
        phone = item.phone,
        vehicleIdText = item.vehicleId,
        visible = "${item.visible}",
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTariffRollupViews(items: MutableList<BatchedTariffRollup>): MutableList<BatchedTariffRollupView> {
    val views = mutableListOf<BatchedTariffRollupView>()
    for (item in items) {
        views.add(toBatchedTariffRollupView(item))
    }
    return views
}

fun sumBatchedTariffRollupView(view: BatchedTariffRollupView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedRetries.toDouble() + parsedCost
}
