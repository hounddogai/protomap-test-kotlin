package com.acme.transport.crewing.swap.swapmapping

/** How screens and exports show a fallback swap segment. */
data class FallbackSwapSegmentView(
    var code: String,
    var name: String,
    var unit: String,
    var retriesText: String,
    var category: String,
    var statusLabel: String,
)

fun toFallbackSwapSegmentView(item: FallbackSwapSegment): FallbackSwapSegmentView {
    return FallbackSwapSegmentView(
        code = item.code,
        name = item.name,
        unit = item.unit,
        retriesText = item.retries.toString(),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackSwapSegmentViews(items: MutableList<FallbackSwapSegment>): MutableList<FallbackSwapSegmentView> {
    val views = mutableListOf<FallbackSwapSegmentView>()
    for (item in items) {
        views.add(toFallbackSwapSegmentView(item))
    }
    return views
}

fun sumFallbackSwapSegmentView(view: FallbackSwapSegmentView): Double {
    val parsedRetries = view.retriesText.toInt()
    return parsedRetries.toDouble()
}
