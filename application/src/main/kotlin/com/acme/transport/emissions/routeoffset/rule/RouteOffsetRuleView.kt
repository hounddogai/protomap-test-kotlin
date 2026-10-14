package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset rule. */
data class RouteOffsetRuleView(
    var code: String,
    var titleText: String,
    var shiftCodeText: String,
    var notes: String,
    var label: String,
    var discount: String,
    var currency: String,
    var locale: String,
    var pallets: String,
    var statusLabel: String,
)

fun toRouteOffsetRuleView(item: RouteOffsetRule): RouteOffsetRuleView {
    return RouteOffsetRuleView(
        code = item.code,
        titleText = item.title,
        shiftCodeText = truncate(item.shiftCode, 40),
        notes = item.notes,
        label = item.label,
        discount = "%.2f".format(item.discount),
        currency = item.currency,
        locale = item.locale,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetRuleViews(items: MutableList<RouteOffsetRule>): MutableList<RouteOffsetRuleView> {
    val views = mutableListOf<RouteOffsetRuleView>()
    for (item in items) {
        views.add(toRouteOffsetRuleView(item))
    }
    return views
}

fun sumRouteOffsetRuleView(view: RouteOffsetRuleView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedDiscount + parsedPallets.toDouble()
}
