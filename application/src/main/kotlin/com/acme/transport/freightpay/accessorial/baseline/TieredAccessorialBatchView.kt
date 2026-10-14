package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered accessorial batch. */
data class TieredAccessorialBatchView(
    var code: String,
    var title: String,
    var lane: String,
    var sequence: String,
    var dock: String,
    var revision: String,
    var version: String,
    var amountText: String,
    var statusLabel: String,
)

fun toTieredAccessorialBatchView(item: TieredAccessorialBatch): TieredAccessorialBatchView {
    return TieredAccessorialBatchView(
        code = item.code,
        title = truncate(item.title, 80),
        lane = item.lane,
        sequence = item.sequence.toString(),
        dock = item.dock,
        revision = item.revision.toString(),
        version = item.version.toString(),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredAccessorialBatchViews(items: MutableList<TieredAccessorialBatch>): MutableList<TieredAccessorialBatchView> {
    val views = mutableListOf<TieredAccessorialBatchView>()
    for (item in items) {
        views.add(toTieredAccessorialBatchView(item))
    }
    return views
}

fun sumTieredAccessorialBatchView(view: TieredAccessorialBatchView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedVersion = view.version.toInt()
    return parsedSequence.toDouble() + parsedRevision.toDouble() + parsedVersion.toDouble()
}
