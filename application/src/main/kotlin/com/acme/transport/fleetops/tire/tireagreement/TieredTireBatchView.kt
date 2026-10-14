package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered tire batch. */
data class TieredTireBatchView(
    var code: String,
    var titleText: String,
    var etaMinutes: String,
    var depot: String,
    var pinnedText: String,
    var amount: String,
    var label: String,
    var segment: String,
    var statusLabel: String,
)

fun toTieredTireBatchView(item: TieredTireBatch): TieredTireBatchView {
    return TieredTireBatchView(
        code = truncate(item.code, 64),
        titleText = truncate(item.title, 40),
        etaMinutes = item.etaMinutes.toString(),
        depot = item.depot,
        pinnedText = "${item.pinned}",
        amount = "%.2f".format(item.amount),
        label = item.label,
        segment = truncate(item.segment, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTireBatchViews(items: MutableList<TieredTireBatch>): MutableList<TieredTireBatchView> {
    val views = mutableListOf<TieredTireBatchView>()
    for (item in items) {
        views.add(toTieredTireBatchView(item))
    }
    return views
}

fun sumTieredTireBatchView(view: TieredTireBatchView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedEtaMinutes.toDouble() + parsedAmount
}
