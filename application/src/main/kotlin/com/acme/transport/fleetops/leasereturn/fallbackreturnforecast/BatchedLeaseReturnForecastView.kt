package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched lease return forecast. */
data class BatchedLeaseReturnForecastView(
    var code: String,
    var title: String,
    var version: String,
    var marginText: String,
    var discount: String,
    var quantity: String,
    var priority: String,
    var region: String,
    var statusLabel: String,
)

fun toBatchedLeaseReturnForecastView(item: BatchedLeaseReturnForecast): BatchedLeaseReturnForecastView {
    return BatchedLeaseReturnForecastView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 64),
        version = item.version.toString(),
        marginText = "%.2f".format(item.margin),
        discount = "%.2f".format(item.discount),
        quantity = item.quantity.toString(),
        priority = item.priority.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedLeaseReturnForecastViews(items: MutableList<BatchedLeaseReturnForecast>): MutableList<BatchedLeaseReturnForecastView> {
    val views = mutableListOf<BatchedLeaseReturnForecastView>()
    for (item in items) {
        views.add(toBatchedLeaseReturnForecastView(item))
    }
    return views
}

fun sumBatchedLeaseReturnForecastView(view: BatchedLeaseReturnForecastView): Double {
    val parsedVersion = view.version.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedVersion.toDouble() + parsedMargin + parsedDiscount
}
