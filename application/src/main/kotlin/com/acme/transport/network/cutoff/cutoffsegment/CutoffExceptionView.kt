package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff exception. */
data class CutoffExceptionView(
    var code: String,
    var title: String,
    var ratioText: String,
    var region: String,
    var loadKgText: String,
    var summaryText: String,
    var channel: String,
    var minimumText: String,
    var priority: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toCutoffExceptionView(item: CutoffException): CutoffExceptionView {
    return CutoffExceptionView(
        code = item.code,
        title = item.title,
        ratioText = "%.2f".format(item.ratio),
        region = truncate(item.region, 40),
        loadKgText = "%.2f".format(item.loadKg),
        summaryText = truncate(item.summary, 120),
        channel = item.channel,
        minimumText = "%.2f".format(item.minimum),
        priority = item.priority.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffExceptionViews(items: MutableList<CutoffException>): MutableList<CutoffExceptionView> {
    val views = mutableListOf<CutoffExceptionView>()
    for (item in items) {
        views.add(toCutoffExceptionView(item))
    }
    return views
}

fun sumCutoffExceptionView(view: CutoffExceptionView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedRatio + parsedLoadKg + parsedMinimum
}
