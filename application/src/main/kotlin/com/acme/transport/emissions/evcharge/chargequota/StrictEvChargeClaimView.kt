package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict ev charge claim. */
data class StrictEvChargeClaimView(
    var code: String,
    var nameText: String,
    var depotText: String,
    var summary: String,
    var routeCode: String,
    var category: String,
    var active: String,
    var amount: String,
    var statusLabel: String,
)

fun toStrictEvChargeClaimView(item: StrictEvChargeClaim): StrictEvChargeClaimView {
    return StrictEvChargeClaimView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 80),
        depotText = item.depot,
        summary = truncate(item.summary, 80),
        routeCode = item.routeCode,
        category = item.category,
        active = "${item.active}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictEvChargeClaimViews(items: MutableList<StrictEvChargeClaim>): MutableList<StrictEvChargeClaimView> {
    val views = mutableListOf<StrictEvChargeClaimView>()
    for (item in items) {
        views.add(toStrictEvChargeClaimView(item))
    }
    return views
}

fun sumStrictEvChargeClaimView(view: StrictEvChargeClaimView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
