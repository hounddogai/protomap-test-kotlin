package com.acme.transport.linehaul.relay.summary

/** How screens and exports show a relay summary. */
data class RelaySummaryView(
    var code: String,
    var nameText: String,
    var dock: String,
    var channel: String,
    var shiftCode: String,
    var externalRef: String,
    var quantity: String,
    var statusLabel: String,
)

fun toRelaySummaryView(item: RelaySummary): RelaySummaryView {
    return RelaySummaryView(
        code = item.code,
        nameText = item.name,
        dock = item.dock,
        channel = item.channel,
        shiftCode = item.shiftCode,
        externalRef = item.externalRef,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelaySummaryViews(items: MutableList<RelaySummary>): MutableList<RelaySummaryView> {
    val views = mutableListOf<RelaySummaryView>()
    for (item in items) {
        views.add(toRelaySummaryView(item))
    }
    return views
}

fun sumRelaySummaryView(view: RelaySummaryView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
