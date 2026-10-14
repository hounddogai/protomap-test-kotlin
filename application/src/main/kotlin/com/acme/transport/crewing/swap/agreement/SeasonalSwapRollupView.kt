package com.acme.transport.crewing.swap.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal swap rollup. */
data class SeasonalSwapRollupView(
    var code: String,
    var title: String,
    var version: String,
    var threshold: String,
    var dock: String,
    var attempts: String,
    var quantityText: String,
    var visible: String,
    var limit: String,
    var statusLabel: String,
)

fun toSeasonalSwapRollupView(item: SeasonalSwapRollup): SeasonalSwapRollupView {
    return SeasonalSwapRollupView(
        code = item.code,
        title = item.title,
        version = item.version.toString(),
        threshold = "%.2f".format(item.threshold),
        dock = truncate(item.dock, 40),
        attempts = item.attempts.toString(),
        quantityText = item.quantity.toString(),
        visible = "${item.visible}",
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalSwapRollupViews(items: MutableList<SeasonalSwapRollup>): MutableList<SeasonalSwapRollupView> {
    val views = mutableListOf<SeasonalSwapRollupView>()
    for (item in items) {
        views.add(toSeasonalSwapRollupView(item))
    }
    return views
}

fun sumSeasonalSwapRollupView(view: SeasonalSwapRollupView): Double {
    val parsedVersion = view.version.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedVersion.toDouble() + parsedThreshold + parsedAttempts.toDouble()
}
