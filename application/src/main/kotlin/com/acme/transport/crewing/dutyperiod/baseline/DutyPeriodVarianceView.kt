package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period variance. */
data class DutyPeriodVarianceView(
    var code: String,
    var name: String,
    var version: String,
    var statusLabel: String,
)

fun toDutyPeriodVarianceView(item: DutyPeriodVariance): DutyPeriodVarianceView {
    return DutyPeriodVarianceView(
        code = item.code,
        name = truncate(item.name, 80),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodVarianceViews(items: MutableList<DutyPeriodVariance>): MutableList<DutyPeriodVarianceView> {
    val views = mutableListOf<DutyPeriodVarianceView>()
    for (item in items) {
        views.add(toDutyPeriodVarianceView(item))
    }
    return views
}

fun sumDutyPeriodVarianceView(view: DutyPeriodVarianceView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
