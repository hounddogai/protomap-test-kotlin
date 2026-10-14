package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal locker rule. */
data class SeasonalLockerRuleView(
    var code: String,
    var title: String,
    var version: String,
    var segment: String,
    var limit: String,
    var summaryText: String,
    var maximumText: String,
    var channel: String,
    var currency: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toSeasonalLockerRuleView(item: SeasonalLockerRule): SeasonalLockerRuleView {
    return SeasonalLockerRuleView(
        code = truncate(item.code, 80),
        title = item.title,
        version = item.version.toString(),
        segment = truncate(item.segment, 80),
        limit = item.limit.toString(),
        summaryText = truncate(item.summary, 64),
        maximumText = "%.2f".format(item.maximum),
        channel = item.channel,
        currency = item.currency,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalLockerRuleViews(items: MutableList<SeasonalLockerRule>): MutableList<SeasonalLockerRuleView> {
    val views = mutableListOf<SeasonalLockerRuleView>()
    for (item in items) {
        views.add(toSeasonalLockerRuleView(item))
    }
    return views
}

fun sumSeasonalLockerRuleView(view: SeasonalLockerRuleView): Double {
    val parsedVersion = view.version.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedVersion.toDouble() + parsedLimit.toDouble() + parsedMaximum
}
