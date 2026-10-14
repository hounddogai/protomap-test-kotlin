package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback sortation score. */
data class FallbackSortationScoreView(
    var code: String,
    var label: String,
    var trailerId: String,
    var summary: String,
    var maximum: String,
    var visible: String,
    var statusLabel: String,
)

fun toFallbackSortationScoreView(item: FallbackSortationScore): FallbackSortationScoreView {
    return FallbackSortationScoreView(
        code = item.code,
        label = truncate(item.label, 40),
        trailerId = item.trailerId,
        summary = item.summary,
        maximum = "%.2f".format(item.maximum),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackSortationScoreViews(items: MutableList<FallbackSortationScore>): MutableList<FallbackSortationScoreView> {
    val views = mutableListOf<FallbackSortationScoreView>()
    for (item in items) {
        views.add(toFallbackSortationScoreView(item))
    }
    return views
}

fun sumFallbackSortationScoreView(view: FallbackSortationScoreView): Double {
    val parsedMaximum = view.maximum.toDouble()
    return parsedMaximum
}
