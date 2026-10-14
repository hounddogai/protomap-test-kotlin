package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card mapping. */
data class RateCardMappingView(
    var code: String,
    var nameText: String,
    var timezone: String,
    var attemptsText: String,
    var cost: String,
    var category: String,
    var windowDays: String,
    var version: String,
    var titleText: String,
    var statusLabel: String,
)

fun toRateCardMappingView(item: RateCardMapping): RateCardMappingView {
    return RateCardMappingView(
        code = item.code,
        nameText = item.name,
        timezone = truncate(item.timezone, 120),
        attemptsText = item.attempts.toString(),
        cost = "%.2f".format(item.cost),
        category = item.category,
        windowDays = item.windowDays.toString(),
        version = item.version.toString(),
        titleText = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardMappingViews(items: MutableList<RateCardMapping>): MutableList<RateCardMappingView> {
    val views = mutableListOf<RateCardMappingView>()
    for (item in items) {
        views.add(toRateCardMappingView(item))
    }
    return views
}

fun sumRateCardMappingView(view: RateCardMappingView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedAttempts.toDouble() + parsedCost + parsedWindowDays.toDouble()
}
