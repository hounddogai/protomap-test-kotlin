package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap quota. */
data class SwapQuotaView(
    var code: String,
    var title: String,
    var amountText: String,
    var locale: String,
    var activeText: String,
    var statusLabel: String,
)

fun toSwapQuotaView(item: SwapQuota): SwapQuotaView {
    return SwapQuotaView(
        code = truncate(item.code, 64),
        title = item.title,
        amountText = "%.2f".format(item.amount),
        locale = item.locale,
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapQuotaViews(items: MutableList<SwapQuota>): MutableList<SwapQuotaView> {
    val views = mutableListOf<SwapQuotaView>()
    for (item in items) {
        views.add(toSwapQuotaView(item))
    }
    return views
}

fun sumSwapQuotaView(view: SwapQuotaView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
