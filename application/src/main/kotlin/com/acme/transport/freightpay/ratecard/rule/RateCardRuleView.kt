package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card rule. */
data class RateCardRuleView(
    var code: String,
    var labelText: String,
    var depot: String,
    var costText: String,
    var dock: String,
    var statusLabel: String,
)

fun toRateCardRuleView(item: RateCardRule): RateCardRuleView {
    return RateCardRuleView(
        code = item.code,
        labelText = item.label,
        depot = truncate(item.depot, 40),
        costText = "%.2f".format(item.cost),
        dock = truncate(item.dock, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardRuleViews(items: MutableList<RateCardRule>): MutableList<RateCardRuleView> {
    val views = mutableListOf<RateCardRuleView>()
    for (item in items) {
        views.add(toRateCardRuleView(item))
    }
    return views
}

fun sumRateCardRuleView(view: RateCardRuleView): Double {
    val parsedCost = view.costText.toDouble()
    return parsedCost
}
