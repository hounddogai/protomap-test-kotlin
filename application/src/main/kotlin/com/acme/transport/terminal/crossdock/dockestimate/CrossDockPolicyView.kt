package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock policy. */
data class CrossDockPolicyView(
    var code: String,
    var titleText: String,
    var floorValue: String,
    var sequence: String,
    var currencyText: String,
    var threshold: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toCrossDockPolicyView(item: CrossDockPolicy): CrossDockPolicyView {
    return CrossDockPolicyView(
        code = truncate(item.code, 80),
        titleText = item.title,
        floorValue = "%.2f".format(item.floorValue),
        sequence = item.sequence.toString(),
        currencyText = item.currency,
        threshold = "%.2f".format(item.threshold),
        trailerIdText = truncate(item.trailerId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockPolicyViews(items: MutableList<CrossDockPolicy>): MutableList<CrossDockPolicyView> {
    val views = mutableListOf<CrossDockPolicyView>()
    for (item in items) {
        views.add(toCrossDockPolicyView(item))
    }
    return views
}

fun sumCrossDockPolicyView(view: CrossDockPolicyView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedFloorValue + parsedSequence.toDouble() + parsedThreshold
}
