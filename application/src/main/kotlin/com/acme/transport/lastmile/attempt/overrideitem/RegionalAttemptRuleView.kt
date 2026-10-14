package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional attempt rule. */
data class RegionalAttemptRuleView(
    var codeText: String,
    var nameText: String,
    var pallets: String,
    var minimumText: String,
    var dock: String,
    var attemptsText: String,
    var categoryText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toRegionalAttemptRuleView(item: RegionalAttemptRule): RegionalAttemptRuleView {
    return RegionalAttemptRuleView(
        codeText = item.code,
        nameText = item.name,
        pallets = item.pallets.toString(),
        minimumText = "%.2f".format(item.minimum),
        dock = truncate(item.dock, 64),
        attemptsText = item.attempts.toString(),
        categoryText = item.category,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalAttemptRuleViews(items: MutableList<RegionalAttemptRule>): MutableList<RegionalAttemptRuleView> {
    val views = mutableListOf<RegionalAttemptRuleView>()
    for (item in items) {
        views.add(toRegionalAttemptRuleView(item))
    }
    return views
}

fun sumRegionalAttemptRuleView(view: RegionalAttemptRuleView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedPallets.toDouble() + parsedMinimum + parsedAttempts.toDouble()
}
