package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell group. */
data class DwellGroupView(
    var code: String,
    var nameText: String,
    var batchSize: String,
    var marginText: String,
    var stopCount: String,
    var currencyText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toDwellGroupView(item: DwellGroup): DwellGroupView {
    return DwellGroupView(
        code = item.code,
        nameText = item.name,
        batchSize = item.batchSize.toString(),
        marginText = "%.2f".format(item.margin),
        stopCount = item.stopCount.toString(),
        currencyText = truncate(item.currency, 40),
        carrier = truncate(item.carrier, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellGroupViews(items: MutableList<DwellGroup>): MutableList<DwellGroupView> {
    val views = mutableListOf<DwellGroupView>()
    for (item in items) {
        views.add(toDwellGroupView(item))
    }
    return views
}

fun sumDwellGroupView(view: DwellGroupView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedBatchSize.toDouble() + parsedMargin + parsedStopCount.toDouble()
}
