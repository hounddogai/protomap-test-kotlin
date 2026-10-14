package com.acme.transport.network.transittime.timerequest

/** How screens and exports show a lenient transit time request. */
data class LenientTransitTimeRequestView(
    var code: String,
    var titleText: String,
    var priority: String,
    var categoryText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toLenientTransitTimeRequestView(item: LenientTransitTimeRequest): LenientTransitTimeRequestView {
    return LenientTransitTimeRequestView(
        code = item.code,
        titleText = item.title,
        priority = item.priority.toString(),
        categoryText = item.category,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTransitTimeRequestViews(items: MutableList<LenientTransitTimeRequest>): MutableList<LenientTransitTimeRequestView> {
    val views = mutableListOf<LenientTransitTimeRequestView>()
    for (item in items) {
        views.add(toLenientTransitTimeRequestView(item))
    }
    return views
}

fun sumLenientTransitTimeRequestView(view: LenientTransitTimeRequestView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedPriority.toDouble() + parsedUpdatedAt.toDouble()
}
