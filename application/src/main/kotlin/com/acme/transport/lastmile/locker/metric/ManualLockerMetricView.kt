package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual locker metric. */
data class ManualLockerMetricView(
    var codeText: String,
    var title: String,
    var visible: String,
    var etaMinutes: String,
    var shiftCode: String,
    var source: String,
    var unit: String,
    var dock: String,
    var statusLabel: String,
)

fun toManualLockerMetricView(item: ManualLockerMetric): ManualLockerMetricView {
    return ManualLockerMetricView(
        codeText = item.code,
        title = item.title,
        visible = "${item.visible}",
        etaMinutes = item.etaMinutes.toString(),
        shiftCode = item.shiftCode,
        source = item.source,
        unit = truncate(item.unit, 120),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualLockerMetricViews(items: MutableList<ManualLockerMetric>): MutableList<ManualLockerMetricView> {
    val views = mutableListOf<ManualLockerMetricView>()
    for (item in items) {
        views.add(toManualLockerMetricView(item))
    }
    return views
}

fun sumManualLockerMetricView(view: ManualLockerMetricView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
