package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered drop adjustment. */
data class TieredDropAdjustmentView(
    var code: String,
    var name: String,
    var ratioText: String,
    var batchSizeText: String,
    var description: String,
    var statusLabel: String,
)

fun toTieredDropAdjustmentView(item: TieredDropAdjustment): TieredDropAdjustmentView {
    return TieredDropAdjustmentView(
        code = truncate(item.code, 120),
        name = item.name,
        ratioText = "%.2f".format(item.ratio),
        batchSizeText = item.batchSize.toString(),
        description = truncate(item.description, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDropAdjustmentViews(items: MutableList<TieredDropAdjustment>): MutableList<TieredDropAdjustmentView> {
    val views = mutableListOf<TieredDropAdjustmentView>()
    for (item in items) {
        views.add(toTieredDropAdjustmentView(item))
    }
    return views
}

fun sumTieredDropAdjustmentView(view: TieredDropAdjustmentView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedRatio + parsedBatchSize.toDouble()
}
