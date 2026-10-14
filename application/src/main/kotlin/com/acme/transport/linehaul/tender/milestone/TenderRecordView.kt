package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender record. */
data class TenderRecordView(
    var code: String,
    var title: String,
    var country: String,
    var budget: String,
    var segment: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toTenderRecordView(item: TenderRecord): TenderRecordView {
    return TenderRecordView(
        code = truncate(item.code, 80),
        title = item.title,
        country = item.country,
        budget = "%.2f".format(item.budget),
        segment = truncate(item.segment, 120),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderRecordViews(items: MutableList<TenderRecord>): MutableList<TenderRecordView> {
    val views = mutableListOf<TenderRecordView>()
    for (item in items) {
        views.add(toTenderRecordView(item))
    }
    return views
}

fun sumTenderRecordView(view: TenderRecordView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedBudget + parsedStopCount.toDouble()
}
