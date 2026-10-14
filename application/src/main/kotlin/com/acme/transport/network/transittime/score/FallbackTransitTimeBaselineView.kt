package com.acme.transport.network.transittime.score

/** How screens and exports show a fallback transit time baseline. */
data class FallbackTransitTimeBaselineView(
    var code: String,
    var name: String,
    var locale: String,
    var label: String,
    var stopCountText: String,
    var retries: String,
    var sequence: String,
    var rate: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toFallbackTransitTimeBaselineView(item: FallbackTransitTimeBaseline): FallbackTransitTimeBaselineView {
    return FallbackTransitTimeBaselineView(
        code = item.code,
        name = item.name,
        locale = item.locale,
        label = item.label,
        stopCountText = item.stopCount.toString(),
        retries = item.retries.toString(),
        sequence = item.sequence.toString(),
        rate = "%.2f".format(item.rate),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTransitTimeBaselineViews(items: MutableList<FallbackTransitTimeBaseline>): MutableList<FallbackTransitTimeBaselineView> {
    val views = mutableListOf<FallbackTransitTimeBaselineView>()
    for (item in items) {
        views.add(toFallbackTransitTimeBaselineView(item))
    }
    return views
}

fun sumFallbackTransitTimeBaselineView(view: FallbackTransitTimeBaselineView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedStopCount.toDouble() + parsedRetries.toDouble() + parsedSequence.toDouble()
}
