package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge exception. */
data class FuelSurchargeExceptionView(
    var codeText: String,
    var title: String,
    var budget: String,
    var ownerId: String,
    var reviewer: String,
    var region: String,
    var timezone: String,
    var segment: String,
    var statusLabel: String,
)

fun toFuelSurchargeExceptionView(item: FuelSurchargeException): FuelSurchargeExceptionView {
    return FuelSurchargeExceptionView(
        codeText = item.code,
        title = truncate(item.title, 80),
        budget = "%.2f".format(item.budget),
        ownerId = item.ownerId,
        reviewer = item.reviewer,
        region = item.region,
        timezone = item.timezone,
        segment = truncate(item.segment, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeExceptionViews(items: MutableList<FuelSurchargeException>): MutableList<FuelSurchargeExceptionView> {
    val views = mutableListOf<FuelSurchargeExceptionView>()
    for (item in items) {
        views.add(toFuelSurchargeExceptionView(item))
    }
    return views
}

fun sumFuelSurchargeExceptionView(view: FuelSurchargeExceptionView): Double {
    val parsedBudget = view.budget.toDouble()
    return parsedBudget
}
