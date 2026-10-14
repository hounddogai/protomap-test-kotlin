package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery quota. */
data class RedeliveryQuotaView(
    var code: String,
    var label: String,
    var cost: String,
    var graceDays: String,
    var amount: String,
    var statusLabel: String,
)

fun toRedeliveryQuotaView(item: RedeliveryQuota): RedeliveryQuotaView {
    return RedeliveryQuotaView(
        code = truncate(item.code, 120),
        label = item.label,
        cost = "%.2f".format(item.cost),
        graceDays = item.graceDays.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryQuotaViews(items: MutableList<RedeliveryQuota>): MutableList<RedeliveryQuotaView> {
    val views = mutableListOf<RedeliveryQuotaView>()
    for (item in items) {
        views.add(toRedeliveryQuotaView(item))
    }
    return views
}

fun sumRedeliveryQuotaView(view: RedeliveryQuotaView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedCost + parsedGraceDays.toDouble() + parsedAmount
}
