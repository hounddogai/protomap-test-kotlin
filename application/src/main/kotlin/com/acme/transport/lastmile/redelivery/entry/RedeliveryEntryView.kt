package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery entry. */
data class RedeliveryEntryView(
    var code: String,
    var title: String,
    var stopCountText: String,
    var localeText: String,
    var amount: String,
    var statusLabel: String,
)

fun toRedeliveryEntryView(item: RedeliveryEntry): RedeliveryEntryView {
    return RedeliveryEntryView(
        code = item.code,
        title = item.title,
        stopCountText = item.stopCount.toString(),
        localeText = truncate(item.locale, 64),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryEntryViews(items: MutableList<RedeliveryEntry>): MutableList<RedeliveryEntryView> {
    val views = mutableListOf<RedeliveryEntryView>()
    for (item in items) {
        views.add(toRedeliveryEntryView(item))
    }
    return views
}

fun sumRedeliveryEntryView(view: RedeliveryEntryView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedStopCount.toDouble() + parsedAmount
}
