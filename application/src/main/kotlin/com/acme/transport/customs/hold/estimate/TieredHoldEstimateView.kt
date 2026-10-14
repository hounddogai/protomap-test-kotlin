package com.acme.transport.customs.hold.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered hold estimate. */
data class TieredHoldEstimateView(
    var code: String,
    var name: String,
    var priorityText: String,
    var activeText: String,
    var sequenceText: String,
    var limit: String,
    var threshold: String,
    var externalRefText: String,
    var routeCode: String,
    var visible: String,
    var carrier: String,
    var statusLabel: String,
)

fun toTieredHoldEstimateView(item: TieredHoldEstimate): TieredHoldEstimateView {
    return TieredHoldEstimateView(
        code = item.code,
        name = truncate(item.name, 80),
        priorityText = item.priority.toString(),
        activeText = "${item.active}",
        sequenceText = item.sequence.toString(),
        limit = item.limit.toString(),
        threshold = "%.2f".format(item.threshold),
        externalRefText = item.externalRef,
        routeCode = item.routeCode,
        visible = "${item.visible}",
        carrier = truncate(item.carrier, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredHoldEstimateViews(items: MutableList<TieredHoldEstimate>): MutableList<TieredHoldEstimateView> {
    val views = mutableListOf<TieredHoldEstimateView>()
    for (item in items) {
        views.add(toTieredHoldEstimateView(item))
    }
    return views
}

fun sumTieredHoldEstimateView(view: TieredHoldEstimateView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedPriority.toDouble() + parsedSequence.toDouble() + parsedLimit.toDouble()
}
