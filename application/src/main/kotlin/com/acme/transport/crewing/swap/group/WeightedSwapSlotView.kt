package com.acme.transport.crewing.swap.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted swap slot. */
data class WeightedSwapSlotView(
    var code: String,
    var name: String,
    var limit: String,
    var checksum: String,
    var version: String,
    var statusLabel: String,
)

fun toWeightedSwapSlotView(item: WeightedSwapSlot): WeightedSwapSlotView {
    return WeightedSwapSlotView(
        code = item.code,
        name = truncate(item.name, 40),
        limit = item.limit.toString(),
        checksum = truncate(item.checksum, 64),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedSwapSlotViews(items: MutableList<WeightedSwapSlot>): MutableList<WeightedSwapSlotView> {
    val views = mutableListOf<WeightedSwapSlotView>()
    for (item in items) {
        views.add(toWeightedSwapSlotView(item))
    }
    return views
}

fun sumWeightedSwapSlotView(view: WeightedSwapSlotView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedVersion = view.version.toInt()
    return parsedLimit.toDouble() + parsedVersion.toDouble()
}
