package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient cross dock baseline. */
data class LenientCrossDockBaselineView(
    var code: String,
    var name: String,
    var budgetText: String,
    var margin: String,
    var laneText: String,
    var sequenceText: String,
    var phone: String,
    var depot: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toLenientCrossDockBaselineView(item: LenientCrossDockBaseline): LenientCrossDockBaselineView {
    return LenientCrossDockBaselineView(
        code = item.code,
        name = truncate(item.name, 40),
        budgetText = "%.2f".format(item.budget),
        margin = "%.2f".format(item.margin),
        laneText = item.lane,
        sequenceText = item.sequence.toString(),
        phone = item.phone,
        depot = item.depot,
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCrossDockBaselineViews(items: MutableList<LenientCrossDockBaseline>): MutableList<LenientCrossDockBaselineView> {
    val views = mutableListOf<LenientCrossDockBaselineView>()
    for (item in items) {
        views.add(toLenientCrossDockBaselineView(item))
    }
    return views
}

fun sumLenientCrossDockBaselineView(view: LenientCrossDockBaselineView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedBudget + parsedMargin + parsedSequence.toDouble()
}
