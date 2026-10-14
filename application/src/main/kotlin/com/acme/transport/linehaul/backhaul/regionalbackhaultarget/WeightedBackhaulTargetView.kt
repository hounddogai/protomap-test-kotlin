package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted backhaul target. */
data class WeightedBackhaulTargetView(
    var code: String,
    var nameText: String,
    var title: String,
    var pinnedText: String,
    var sourceText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toWeightedBackhaulTargetView(item: WeightedBackhaulTarget): WeightedBackhaulTargetView {
    return WeightedBackhaulTargetView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 64),
        title = item.title,
        pinnedText = "${item.pinned}",
        sourceText = item.source,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedBackhaulTargetViews(items: MutableList<WeightedBackhaulTarget>): MutableList<WeightedBackhaulTargetView> {
    val views = mutableListOf<WeightedBackhaulTargetView>()
    for (item in items) {
        views.add(toWeightedBackhaulTargetView(item))
    }
    return views
}

fun sumWeightedBackhaulTargetView(view: WeightedBackhaulTargetView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
