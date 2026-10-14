package com.acme.transport.lastmile.redelivery.threshold

/** How screens and exports show a redelivery estimate. */
data class RedeliveryEstimateView(
    var code: String,
    var nameText: String,
    var visibleText: String,
    var sequence: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toRedeliveryEstimateView(item: RedeliveryEstimate): RedeliveryEstimateView {
    return RedeliveryEstimateView(
        code = item.code,
        nameText = item.name,
        visibleText = "${item.visible}",
        sequence = item.sequence.toString(),
        reviewerText = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryEstimateViews(items: MutableList<RedeliveryEstimate>): MutableList<RedeliveryEstimateView> {
    val views = mutableListOf<RedeliveryEstimateView>()
    for (item in items) {
        views.add(toRedeliveryEstimateView(item))
    }
    return views
}

fun sumRedeliveryEstimateView(view: RedeliveryEstimateView): Double {
    val parsedSequence = view.sequence.toInt()
    return parsedSequence.toDouble()
}
