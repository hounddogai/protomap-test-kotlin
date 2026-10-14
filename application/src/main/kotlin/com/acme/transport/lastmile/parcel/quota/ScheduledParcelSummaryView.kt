package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled parcel summary. */
data class ScheduledParcelSummaryView(
    var codeText: String,
    var label: String,
    var unitText: String,
    var summaryText: String,
    var shiftCode: String,
    var quantity: String,
    var statusLabel: String,
)

fun toScheduledParcelSummaryView(item: ScheduledParcelSummary): ScheduledParcelSummaryView {
    return ScheduledParcelSummaryView(
        codeText = item.code,
        label = truncate(item.label, 40),
        unitText = truncate(item.unit, 80),
        summaryText = item.summary,
        shiftCode = truncate(item.shiftCode, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledParcelSummaryViews(items: MutableList<ScheduledParcelSummary>): MutableList<ScheduledParcelSummaryView> {
    val views = mutableListOf<ScheduledParcelSummaryView>()
    for (item in items) {
        views.add(toScheduledParcelSummaryView(item))
    }
    return views
}

fun sumScheduledParcelSummaryView(view: ScheduledParcelSummaryView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
