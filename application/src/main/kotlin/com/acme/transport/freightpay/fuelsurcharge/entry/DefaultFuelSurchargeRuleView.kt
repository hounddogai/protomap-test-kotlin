package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default fuel surcharge rule. */
data class DefaultFuelSurchargeRuleView(
    var code: String,
    var label: String,
    var country: String,
    var reviewer: String,
    var regionText: String,
    var visibleText: String,
    var depot: String,
    var ownerId: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toDefaultFuelSurchargeRuleView(item: DefaultFuelSurchargeRule): DefaultFuelSurchargeRuleView {
    return DefaultFuelSurchargeRuleView(
        code = item.code,
        label = item.label,
        country = item.country,
        reviewer = truncate(item.reviewer, 120),
        regionText = item.region,
        visibleText = "${item.visible}",
        depot = truncate(item.depot, 120),
        ownerId = truncate(item.ownerId, 120),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultFuelSurchargeRuleViews(items: MutableList<DefaultFuelSurchargeRule>): MutableList<DefaultFuelSurchargeRuleView> {
    val views = mutableListOf<DefaultFuelSurchargeRuleView>()
    for (item in items) {
        views.add(toDefaultFuelSurchargeRuleView(item))
    }
    return views
}

fun sumDefaultFuelSurchargeRuleView(view: DefaultFuelSurchargeRuleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
