package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling backhaul batch. */
data class RollingBackhaulBatchView(
    var code: String,
    var name: String,
    var phoneText: String,
    var sourceText: String,
    var localeText: String,
    var reviewer: String,
    var quantity: String,
    var statusLabel: String,
)

fun toRollingBackhaulBatchView(item: RollingBackhaulBatch): RollingBackhaulBatchView {
    return RollingBackhaulBatchView(
        code = item.code,
        name = truncate(item.name, 120),
        phoneText = item.phone,
        sourceText = item.source,
        localeText = item.locale,
        reviewer = item.reviewer,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingBackhaulBatchViews(items: MutableList<RollingBackhaulBatch>): MutableList<RollingBackhaulBatchView> {
    val views = mutableListOf<RollingBackhaulBatchView>()
    for (item in items) {
        views.add(toRollingBackhaulBatchView(item))
    }
    return views
}

fun sumRollingBackhaulBatchView(view: RollingBackhaulBatchView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
