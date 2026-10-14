package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell snapshot. */
data class DwellSnapshotView(
    var code: String,
    var label: String,
    var endsAtText: String,
    var title: String,
    var quantity: String,
    var batchSize: String,
    var approved: String,
    var retries: String,
    var limit: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toDwellSnapshotView(item: DwellSnapshot): DwellSnapshotView {
    return DwellSnapshotView(
        code = truncate(item.code, 120),
        label = item.label,
        endsAtText = item.endsAt.toString(),
        title = item.title,
        quantity = item.quantity.toString(),
        batchSize = item.batchSize.toString(),
        approved = "${item.approved}",
        retries = item.retries.toString(),
        limit = item.limit.toString(),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellSnapshotViews(items: MutableList<DwellSnapshot>): MutableList<DwellSnapshotView> {
    val views = mutableListOf<DwellSnapshotView>()
    for (item in items) {
        views.add(toDwellSnapshotView(item))
    }
    return views
}

fun sumDwellSnapshotView(view: DwellSnapshotView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedEndsAt.toDouble() + parsedQuantity.toDouble() + parsedBatchSize.toDouble()
}
