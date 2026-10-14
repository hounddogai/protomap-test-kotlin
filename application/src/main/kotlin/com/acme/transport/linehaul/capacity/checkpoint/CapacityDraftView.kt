package com.acme.transport.linehaul.capacity.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity draft. */
data class CapacityDraftView(
    var code: String,
    var title: String,
    var pinned: String,
    var unit: String,
    var channel: String,
    var amountText: String,
    var statusLabel: String,
)

fun toCapacityDraftView(item: CapacityDraft): CapacityDraftView {
    return CapacityDraftView(
        code = item.code,
        title = item.title,
        pinned = "${item.pinned}",
        unit = truncate(item.unit, 80),
        channel = item.channel,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityDraftViews(items: MutableList<CapacityDraft>): MutableList<CapacityDraftView> {
    val views = mutableListOf<CapacityDraftView>()
    for (item in items) {
        views.add(toCapacityDraftView(item))
    }
    return views
}

fun sumCapacityDraftView(view: CapacityDraftView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
