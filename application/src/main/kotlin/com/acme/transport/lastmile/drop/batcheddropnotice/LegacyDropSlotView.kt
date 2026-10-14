package com.acme.transport.lastmile.drop.batcheddropnotice

/** How screens and exports show a legacy drop slot. */
data class LegacyDropSlotView(
    var code: String,
    var title: String,
    var ceilingText: String,
    var archived: String,
    var countryText: String,
    var statusLabel: String,
)

fun toLegacyDropSlotView(item: LegacyDropSlot): LegacyDropSlotView {
    return LegacyDropSlotView(
        code = item.code,
        title = item.title,
        ceilingText = "%.2f".format(item.ceiling),
        archived = "${item.archived}",
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDropSlotViews(items: MutableList<LegacyDropSlot>): MutableList<LegacyDropSlotView> {
    val views = mutableListOf<LegacyDropSlotView>()
    for (item in items) {
        views.add(toLegacyDropSlotView(item))
    }
    return views
}

fun sumLegacyDropSlotView(view: LegacyDropSlotView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedCeiling
}
