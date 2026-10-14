package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched broker claim. */
data class BatchedBrokerClaimView(
    var code: String,
    var label: String,
    var regionText: String,
    var routeCode: String,
    var currency: String,
    var endsAtText: String,
    var segment: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toBatchedBrokerClaimView(item: BatchedBrokerClaim): BatchedBrokerClaimView {
    return BatchedBrokerClaimView(
        code = item.code,
        label = truncate(item.label, 64),
        regionText = item.region,
        routeCode = item.routeCode,
        currency = item.currency,
        endsAtText = item.endsAt.toString(),
        segment = item.segment,
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedBrokerClaimViews(items: MutableList<BatchedBrokerClaim>): MutableList<BatchedBrokerClaimView> {
    val views = mutableListOf<BatchedBrokerClaimView>()
    for (item in items) {
        views.add(toBatchedBrokerClaimView(item))
    }
    return views
}

fun sumBatchedBrokerClaimView(view: BatchedBrokerClaimView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedEndsAt.toDouble() + parsedFloorValue
}
