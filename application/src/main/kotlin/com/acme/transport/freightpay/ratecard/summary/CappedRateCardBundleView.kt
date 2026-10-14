package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped rate card bundle. */
data class CappedRateCardBundleView(
    var codeText: String,
    var title: String,
    var loadKgText: String,
    var startsAt: String,
    var category: String,
    var fuelLitersText: String,
    var notesText: String,
    var score: String,
    var rate: String,
    var expiresAt: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toCappedRateCardBundleView(item: CappedRateCardBundle): CappedRateCardBundleView {
    return CappedRateCardBundleView(
        codeText = item.code,
        title = item.title,
        loadKgText = "%.2f".format(item.loadKg),
        startsAt = item.startsAt.toString(),
        category = item.category,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        notesText = truncate(item.notes, 40),
        score = "%.2f".format(item.score),
        rate = "%.2f".format(item.rate),
        expiresAt = item.expiresAt.toString(),
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRateCardBundleViews(items: MutableList<CappedRateCardBundle>): MutableList<CappedRateCardBundleView> {
    val views = mutableListOf<CappedRateCardBundleView>()
    for (item in items) {
        views.add(toCappedRateCardBundleView(item))
    }
    return views
}

fun sumCappedRateCardBundleView(view: CappedRateCardBundleView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedLoadKg + parsedStartsAt.toDouble() + parsedFuelLiters
}
