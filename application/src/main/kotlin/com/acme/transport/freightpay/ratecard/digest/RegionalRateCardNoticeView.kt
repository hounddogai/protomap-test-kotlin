package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional rate card notice. */
data class RegionalRateCardNoticeView(
    var code: String,
    var nameText: String,
    var budget: String,
    var priority: String,
    var minimum: String,
    var floorValue: String,
    var sequenceText: String,
    var label: String,
    var amount: String,
    var notes: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toRegionalRateCardNoticeView(item: RegionalRateCardNotice): RegionalRateCardNoticeView {
    return RegionalRateCardNoticeView(
        code = item.code,
        nameText = item.name,
        budget = "%.2f".format(item.budget),
        priority = item.priority.toString(),
        minimum = "%.2f".format(item.minimum),
        floorValue = "%.2f".format(item.floorValue),
        sequenceText = item.sequence.toString(),
        label = item.label,
        amount = "%.2f".format(item.amount),
        notes = truncate(item.notes, 40),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRateCardNoticeViews(items: MutableList<RegionalRateCardNotice>): MutableList<RegionalRateCardNoticeView> {
    val views = mutableListOf<RegionalRateCardNoticeView>()
    for (item in items) {
        views.add(toRegionalRateCardNoticeView(item))
    }
    return views
}

fun sumRegionalRateCardNoticeView(view: RegionalRateCardNoticeView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedBudget + parsedPriority.toDouble() + parsedMinimum
}
