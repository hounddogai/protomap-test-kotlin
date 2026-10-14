package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery claim. */
data class RedeliveryClaimView(
    var codeText: String,
    var labelText: String,
    var lane: String,
    var batchSize: String,
    var trailerId: String,
    var region: String,
    var title: String,
    var rate: String,
    var reviewer: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toRedeliveryClaimView(item: RedeliveryClaim): RedeliveryClaimView {
    return RedeliveryClaimView(
        codeText = item.code,
        labelText = item.label,
        lane = truncate(item.lane, 40),
        batchSize = item.batchSize.toString(),
        trailerId = truncate(item.trailerId, 80),
        region = item.region,
        title = truncate(item.title, 80),
        rate = "%.2f".format(item.rate),
        reviewer = truncate(item.reviewer, 80),
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryClaimViews(items: MutableList<RedeliveryClaim>): MutableList<RedeliveryClaimView> {
    val views = mutableListOf<RedeliveryClaimView>()
    for (item in items) {
        views.add(toRedeliveryClaimView(item))
    }
    return views
}

fun sumRedeliveryClaimView(view: RedeliveryClaimView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedBatchSize.toDouble() + parsedRate + parsedLoadKg
}
