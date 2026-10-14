package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary cutoff batch. */
data class PrimaryCutoffBatchView(
    var codeText: String,
    var name: String,
    var marginText: String,
    var etaMinutesText: String,
    var visible: String,
    var statusLabel: String,
)

fun toPrimaryCutoffBatchView(item: PrimaryCutoffBatch): PrimaryCutoffBatchView {
    return PrimaryCutoffBatchView(
        codeText = truncate(item.code, 120),
        name = item.name,
        marginText = "%.2f".format(item.margin),
        etaMinutesText = item.etaMinutes.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCutoffBatchViews(items: MutableList<PrimaryCutoffBatch>): MutableList<PrimaryCutoffBatchView> {
    val views = mutableListOf<PrimaryCutoffBatchView>()
    for (item in items) {
        views.add(toPrimaryCutoffBatchView(item))
    }
    return views
}

fun sumPrimaryCutoffBatchView(view: PrimaryCutoffBatchView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedMargin + parsedEtaMinutes.toDouble()
}
