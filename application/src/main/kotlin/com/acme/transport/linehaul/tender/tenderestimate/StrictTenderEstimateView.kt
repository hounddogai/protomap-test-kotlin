package com.acme.transport.linehaul.tender.tenderestimate

/** How screens and exports show a strict tender estimate. */
data class StrictTenderEstimateView(
    var codeText: String,
    var title: String,
    var description: String,
    var label: String,
    var pinnedText: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toStrictTenderEstimateView(item: StrictTenderEstimate): StrictTenderEstimateView {
    return StrictTenderEstimateView(
        codeText = item.code,
        title = item.title,
        description = item.description,
        label = item.label,
        pinnedText = "${item.pinned}",
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTenderEstimateViews(items: MutableList<StrictTenderEstimate>): MutableList<StrictTenderEstimateView> {
    val views = mutableListOf<StrictTenderEstimateView>()
    for (item in items) {
        views.add(toStrictTenderEstimateView(item))
    }
    return views
}

fun sumStrictTenderEstimateView(view: StrictTenderEstimateView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedWindowDays.toDouble()
}
