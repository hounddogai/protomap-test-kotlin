package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender profile. */
data class TenderProfileView(
    var code: String,
    var name: String,
    var retriesText: String,
    var endsAt: String,
    var quantity: String,
    var shiftCode: String,
    var summaryText: String,
    var margin: String,
    var pinned: String,
    var statusLabel: String,
)

fun toTenderProfileView(item: TenderProfile): TenderProfileView {
    return TenderProfileView(
        code = item.code,
        name = truncate(item.name, 40),
        retriesText = item.retries.toString(),
        endsAt = item.endsAt.toString(),
        quantity = item.quantity.toString(),
        shiftCode = truncate(item.shiftCode, 40),
        summaryText = item.summary,
        margin = "%.2f".format(item.margin),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderProfileViews(items: MutableList<TenderProfile>): MutableList<TenderProfileView> {
    val views = mutableListOf<TenderProfileView>()
    for (item in items) {
        views.add(toTenderProfileView(item))
    }
    return views
}

fun sumTenderProfileView(view: TenderProfileView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedRetries.toDouble() + parsedEndsAt.toDouble() + parsedQuantity.toDouble()
}
