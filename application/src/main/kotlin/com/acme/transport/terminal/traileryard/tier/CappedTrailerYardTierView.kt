package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped trailer yard tier. */
data class CappedTrailerYardTierView(
    var codeText: String,
    var title: String,
    var ceiling: String,
    var windowDays: String,
    var palletsText: String,
    var notes: String,
    var statusLabel: String,
)

fun toCappedTrailerYardTierView(item: CappedTrailerYardTier): CappedTrailerYardTierView {
    return CappedTrailerYardTierView(
        codeText = item.code,
        title = truncate(item.title, 64),
        ceiling = "%.2f".format(item.ceiling),
        windowDays = item.windowDays.toString(),
        palletsText = item.pallets.toString(),
        notes = truncate(item.notes, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTrailerYardTierViews(items: MutableList<CappedTrailerYardTier>): MutableList<CappedTrailerYardTierView> {
    val views = mutableListOf<CappedTrailerYardTierView>()
    for (item in items) {
        views.add(toCappedTrailerYardTierView(item))
    }
    return views
}

fun sumCappedTrailerYardTierView(view: CappedTrailerYardTierView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedCeiling + parsedWindowDays.toDouble() + parsedPallets.toDouble()
}
