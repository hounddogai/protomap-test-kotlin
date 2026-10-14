package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender slot. */
data class TenderSlotView(
    var code: String,
    var title: String,
    var notes: String,
    var depotText: String,
    var score: String,
    var statusLabel: String,
)

fun toTenderSlotView(item: TenderSlot): TenderSlotView {
    return TenderSlotView(
        code = item.code,
        title = item.title,
        notes = truncate(item.notes, 120),
        depotText = truncate(item.depot, 80),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderSlotViews(items: MutableList<TenderSlot>): MutableList<TenderSlotView> {
    val views = mutableListOf<TenderSlotView>()
    for (item in items) {
        views.add(toTenderSlotView(item))
    }
    return views
}

fun sumTenderSlotView(view: TenderSlotView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
