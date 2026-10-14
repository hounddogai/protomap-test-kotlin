package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default fuel surcharge entry. */
data class DefaultFuelSurchargeEntryView(
    var codeText: String,
    var labelText: String,
    var carrier: String,
    var trailerIdText: String,
    var routeCode: String,
    var score: String,
    var statusLabel: String,
)

fun toDefaultFuelSurchargeEntryView(item: DefaultFuelSurchargeEntry): DefaultFuelSurchargeEntryView {
    return DefaultFuelSurchargeEntryView(
        codeText = item.code,
        labelText = item.label,
        carrier = item.carrier,
        trailerIdText = truncate(item.trailerId, 64),
        routeCode = item.routeCode,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultFuelSurchargeEntryViews(items: MutableList<DefaultFuelSurchargeEntry>): MutableList<DefaultFuelSurchargeEntryView> {
    val views = mutableListOf<DefaultFuelSurchargeEntryView>()
    for (item in items) {
        views.add(toDefaultFuelSurchargeEntryView(item))
    }
    return views
}

fun sumDefaultFuelSurchargeEntryView(view: DefaultFuelSurchargeEntryView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
