package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered sortation batch. */
data class TieredSortationBatchView(
    var code: String,
    var name: String,
    var endsAtText: String,
    var updatedAtText: String,
    var shiftCode: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toTieredSortationBatchView(item: TieredSortationBatch): TieredSortationBatchView {
    return TieredSortationBatchView(
        code = item.code,
        name = truncate(item.name, 120),
        endsAtText = item.endsAt.toString(),
        updatedAtText = item.updatedAt.toString(),
        shiftCode = item.shiftCode,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSortationBatchViews(items: MutableList<TieredSortationBatch>): MutableList<TieredSortationBatchView> {
    val views = mutableListOf<TieredSortationBatchView>()
    for (item in items) {
        views.add(toTieredSortationBatchView(item))
    }
    return views
}

fun sumTieredSortationBatchView(view: TieredSortationBatchView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedEndsAt.toDouble() + parsedUpdatedAt.toDouble() + parsedLoadKg
}
