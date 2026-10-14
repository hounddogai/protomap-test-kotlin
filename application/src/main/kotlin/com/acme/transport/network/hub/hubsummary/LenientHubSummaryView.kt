package com.acme.transport.network.hub.hubsummary

/** How screens and exports show a lenient hub summary. */
data class LenientHubSummaryView(
    var codeText: String,
    var title: String,
    var durationMinutes: String,
    var threshold: String,
    var ratio: String,
    var statusLabel: String,
)

fun toLenientHubSummaryView(item: LenientHubSummary): LenientHubSummaryView {
    return LenientHubSummaryView(
        codeText = item.code,
        title = item.title,
        durationMinutes = item.durationMinutes.toString(),
        threshold = "%.2f".format(item.threshold),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientHubSummaryViews(items: MutableList<LenientHubSummary>): MutableList<LenientHubSummaryView> {
    val views = mutableListOf<LenientHubSummaryView>()
    for (item in items) {
        views.add(toLenientHubSummaryView(item))
    }
    return views
}

fun sumLenientHubSummaryView(view: LenientHubSummaryView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedDurationMinutes.toDouble() + parsedThreshold + parsedRatio
}
