package com.acme.transport.crewing.restbreak.threshold

/** How screens and exports show a tiered rest break threshold. */
data class TieredRestBreakThresholdView(
    var code: String,
    var title: String,
    var minimum: String,
    var active: String,
    var retries: String,
    var windowDays: String,
    var version: String,
    var statusLabel: String,
)

fun toTieredRestBreakThresholdView(item: TieredRestBreakThreshold): TieredRestBreakThresholdView {
    return TieredRestBreakThresholdView(
        code = item.code,
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        active = "${item.active}",
        retries = item.retries.toString(),
        windowDays = item.windowDays.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRestBreakThresholdViews(items: MutableList<TieredRestBreakThreshold>): MutableList<TieredRestBreakThresholdView> {
    val views = mutableListOf<TieredRestBreakThresholdView>()
    for (item in items) {
        views.add(toTieredRestBreakThresholdView(item))
    }
    return views
}

fun sumTieredRestBreakThresholdView(view: TieredRestBreakThresholdView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedMinimum + parsedRetries.toDouble() + parsedWindowDays.toDouble()
}
