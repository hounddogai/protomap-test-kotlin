package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary sortation snapshot. */
data class PrimarySortationSnapshotView(
    var code: String,
    var labelText: String,
    var discount: String,
    var summaryText: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toPrimarySortationSnapshotView(item: PrimarySortationSnapshot): PrimarySortationSnapshotView {
    return PrimarySortationSnapshotView(
        code = item.code,
        labelText = item.label,
        discount = "%.2f".format(item.discount),
        summaryText = truncate(item.summary, 120),
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimarySortationSnapshotViews(items: MutableList<PrimarySortationSnapshot>): MutableList<PrimarySortationSnapshotView> {
    val views = mutableListOf<PrimarySortationSnapshotView>()
    for (item in items) {
        views.add(toPrimarySortationSnapshotView(item))
    }
    return views
}

fun sumPrimarySortationSnapshotView(view: PrimarySortationSnapshotView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedDiscount + parsedUpdatedAt.toDouble()
}
