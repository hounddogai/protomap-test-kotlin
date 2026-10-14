package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial metric. */
data class AccessorialMetricView(
    var codeText: String,
    var label: String,
    var dock: String,
    var phone: String,
    var revision: String,
    var limitText: String,
    var sourceText: String,
    var pinnedText: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toAccessorialMetricView(item: AccessorialMetric): AccessorialMetricView {
    return AccessorialMetricView(
        codeText = item.code,
        label = item.label,
        dock = item.dock,
        phone = truncate(item.phone, 40),
        revision = item.revision.toString(),
        limitText = item.limit.toString(),
        sourceText = truncate(item.source, 40),
        pinnedText = "${item.pinned}",
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialMetricViews(items: MutableList<AccessorialMetric>): MutableList<AccessorialMetricView> {
    val views = mutableListOf<AccessorialMetricView>()
    for (item in items) {
        views.add(toAccessorialMetricView(item))
    }
    return views
}

fun sumAccessorialMetricView(view: AccessorialMetricView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedRevision.toDouble() + parsedLimit.toDouble() + parsedMinimum
}
