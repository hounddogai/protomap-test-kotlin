package com.acme.transport.safety.incident.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global incident batch. */
data class GlobalIncidentBatchView(
    var codeText: String,
    var title: String,
    var loadKg: String,
    var limit: String,
    var rateText: String,
    var reviewer: String,
    var threshold: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toGlobalIncidentBatchView(item: GlobalIncidentBatch): GlobalIncidentBatchView {
    return GlobalIncidentBatchView(
        codeText = truncate(item.code, 40),
        title = item.title,
        loadKg = "%.2f".format(item.loadKg),
        limit = item.limit.toString(),
        rateText = "%.2f".format(item.rate),
        reviewer = truncate(item.reviewer, 80),
        threshold = "%.2f".format(item.threshold),
        timezoneText = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalIncidentBatchViews(items: MutableList<GlobalIncidentBatch>): MutableList<GlobalIncidentBatchView> {
    val views = mutableListOf<GlobalIncidentBatchView>()
    for (item in items) {
        views.add(toGlobalIncidentBatchView(item))
    }
    return views
}

fun sumGlobalIncidentBatchView(view: GlobalIncidentBatchView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedLoadKg + parsedLimit.toDouble() + parsedRate
}
