package com.acme.transport.network.zone.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback zone summary. */
data class FallbackZoneSummaryView(
    var codeText: String,
    var title: String,
    var ceiling: String,
    var discount: String,
    var checksum: String,
    var unitText: String,
    var pinned: String,
    var minimum: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toFallbackZoneSummaryView(item: FallbackZoneSummary): FallbackZoneSummaryView {
    return FallbackZoneSummaryView(
        codeText = truncate(item.code, 120),
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        discount = "%.2f".format(item.discount),
        checksum = item.checksum,
        unitText = item.unit,
        pinned = "${item.pinned}",
        minimum = "%.2f".format(item.minimum),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackZoneSummaryViews(items: MutableList<FallbackZoneSummary>): MutableList<FallbackZoneSummaryView> {
    val views = mutableListOf<FallbackZoneSummaryView>()
    for (item in items) {
        views.add(toFallbackZoneSummaryView(item))
    }
    return views
}

fun sumFallbackZoneSummaryView(view: FallbackZoneSummaryView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedCeiling + parsedDiscount + parsedMinimum
}
