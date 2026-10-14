package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped swap agreement. */
data class CappedSwapAgreementView(
    var code: String,
    var name: String,
    var retries: String,
    var trailerId: String,
    var countryText: String,
    var endsAt: String,
    var ratio: String,
    var category: String,
    var limit: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toCappedSwapAgreementView(item: CappedSwapAgreement): CappedSwapAgreementView {
    return CappedSwapAgreementView(
        code = item.code,
        name = truncate(item.name, 120),
        retries = item.retries.toString(),
        trailerId = truncate(item.trailerId, 40),
        countryText = truncate(item.country, 120),
        endsAt = item.endsAt.toString(),
        ratio = "%.2f".format(item.ratio),
        category = item.category,
        limit = item.limit.toString(),
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedSwapAgreementViews(items: MutableList<CappedSwapAgreement>): MutableList<CappedSwapAgreementView> {
    val views = mutableListOf<CappedSwapAgreementView>()
    for (item in items) {
        views.add(toCappedSwapAgreementView(item))
    }
    return views
}

fun sumCappedSwapAgreementView(view: CappedSwapAgreementView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedRetries.toDouble() + parsedEndsAt.toDouble() + parsedRatio
}
