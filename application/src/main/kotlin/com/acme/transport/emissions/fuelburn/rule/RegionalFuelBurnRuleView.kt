package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional fuel burn rule. */
data class RegionalFuelBurnRuleView(
    var code: String,
    var titleText: String,
    var amountText: String,
    var score: String,
    var active: String,
    var channel: String,
    var ratioText: String,
    var minimum: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toRegionalFuelBurnRuleView(item: RegionalFuelBurnRule): RegionalFuelBurnRuleView {
    return RegionalFuelBurnRuleView(
        code = item.code,
        titleText = item.title,
        amountText = "%.2f".format(item.amount),
        score = "%.2f".format(item.score),
        active = "${item.active}",
        channel = truncate(item.channel, 64),
        ratioText = "%.2f".format(item.ratio),
        minimum = "%.2f".format(item.minimum),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalFuelBurnRuleViews(items: MutableList<RegionalFuelBurnRule>): MutableList<RegionalFuelBurnRuleView> {
    val views = mutableListOf<RegionalFuelBurnRuleView>()
    for (item in items) {
        views.add(toRegionalFuelBurnRuleView(item))
    }
    return views
}

fun sumRegionalFuelBurnRuleView(view: RegionalFuelBurnRuleView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedAmount + parsedScore + parsedRatio
}
