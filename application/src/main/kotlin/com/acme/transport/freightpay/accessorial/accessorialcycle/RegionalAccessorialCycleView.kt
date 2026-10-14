package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional accessorial cycle. */
data class RegionalAccessorialCycleView(
    var code: String,
    var titleText: String,
    var threshold: String,
    var ratioText: String,
    var currencyText: String,
    var lane: String,
    var summary: String,
    var approved: String,
    var statusLabel: String,
)

fun toRegionalAccessorialCycleView(item: RegionalAccessorialCycle): RegionalAccessorialCycleView {
    return RegionalAccessorialCycleView(
        code = truncate(item.code, 120),
        titleText = item.title,
        threshold = "%.2f".format(item.threshold),
        ratioText = "%.2f".format(item.ratio),
        currencyText = truncate(item.currency, 80),
        lane = truncate(item.lane, 80),
        summary = item.summary,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalAccessorialCycleViews(items: MutableList<RegionalAccessorialCycle>): MutableList<RegionalAccessorialCycleView> {
    val views = mutableListOf<RegionalAccessorialCycleView>()
    for (item in items) {
        views.add(toRegionalAccessorialCycleView(item))
    }
    return views
}

fun sumRegionalAccessorialCycleView(view: RegionalAccessorialCycleView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedThreshold + parsedRatio
}
