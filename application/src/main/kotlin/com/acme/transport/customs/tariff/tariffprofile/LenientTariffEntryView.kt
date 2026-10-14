package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient tariff entry. */
data class LenientTariffEntryView(
    var code: String,
    var label: String,
    var budgetText: String,
    var threshold: String,
    var ratio: String,
    var laneText: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toLenientTariffEntryView(item: LenientTariffEntry): LenientTariffEntryView {
    return LenientTariffEntryView(
        code = item.code,
        label = item.label,
        budgetText = "%.2f".format(item.budget),
        threshold = "%.2f".format(item.threshold),
        ratio = "%.2f".format(item.ratio),
        laneText = item.lane,
        summaryText = truncate(item.summary, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTariffEntryViews(items: MutableList<LenientTariffEntry>): MutableList<LenientTariffEntryView> {
    val views = mutableListOf<LenientTariffEntryView>()
    for (item in items) {
        views.add(toLenientTariffEntryView(item))
    }
    return views
}

fun sumLenientTariffEntryView(view: LenientTariffEntryView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedBudget + parsedThreshold + parsedRatio
}
