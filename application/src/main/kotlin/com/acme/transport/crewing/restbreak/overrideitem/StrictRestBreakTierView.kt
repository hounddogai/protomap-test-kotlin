package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict rest break tier. */
data class StrictRestBreakTierView(
    var codeText: String,
    var title: String,
    var ratioText: String,
    var minimum: String,
    var unitText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toStrictRestBreakTierView(item: StrictRestBreakTier): StrictRestBreakTierView {
    return StrictRestBreakTierView(
        codeText = item.code,
        title = truncate(item.title, 80),
        ratioText = "%.2f".format(item.ratio),
        minimum = "%.2f".format(item.minimum),
        unitText = truncate(item.unit, 40),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRestBreakTierViews(items: MutableList<StrictRestBreakTier>): MutableList<StrictRestBreakTierView> {
    val views = mutableListOf<StrictRestBreakTierView>()
    for (item in items) {
        views.add(toStrictRestBreakTierView(item))
    }
    return views
}

fun sumStrictRestBreakTierView(view: StrictRestBreakTierView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedRatio + parsedMinimum + parsedLoadKg
}
