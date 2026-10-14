package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict camera event metric. */
data class StrictCameraEventMetricView(
    var codeText: String,
    var title: String,
    var currency: String,
    var minimum: String,
    var source: String,
    var countryText: String,
    var versionText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toStrictCameraEventMetricView(item: StrictCameraEventMetric): StrictCameraEventMetricView {
    return StrictCameraEventMetricView(
        codeText = item.code,
        title = item.title,
        currency = truncate(item.currency, 40),
        minimum = "%.2f".format(item.minimum),
        source = truncate(item.source, 64),
        countryText = item.country,
        versionText = item.version.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCameraEventMetricViews(items: MutableList<StrictCameraEventMetric>): MutableList<StrictCameraEventMetricView> {
    val views = mutableListOf<StrictCameraEventMetricView>()
    for (item in items) {
        views.add(toStrictCameraEventMetricView(item))
    }
    return views
}

fun sumStrictCameraEventMetricView(view: StrictCameraEventMetricView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedVersion = view.versionText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedMinimum + parsedVersion.toDouble() + parsedDurationMinutes.toDouble()
}
