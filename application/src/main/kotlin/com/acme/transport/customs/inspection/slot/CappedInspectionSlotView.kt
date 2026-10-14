package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped inspection slot. */
data class CappedInspectionSlotView(
    var code: String,
    var name: String,
    var title: String,
    var pinnedText: String,
    var country: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toCappedInspectionSlotView(item: CappedInspectionSlot): CappedInspectionSlotView {
    return CappedInspectionSlotView(
        code = item.code,
        name = item.name,
        title = item.title,
        pinnedText = "${item.pinned}",
        country = truncate(item.country, 80),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedInspectionSlotViews(items: MutableList<CappedInspectionSlot>): MutableList<CappedInspectionSlotView> {
    val views = mutableListOf<CappedInspectionSlotView>()
    for (item in items) {
        views.add(toCappedInspectionSlotView(item))
    }
    return views
}

fun sumCappedInspectionSlotView(view: CappedInspectionSlotView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
