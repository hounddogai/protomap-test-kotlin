package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period threshold. */
data class DutyPeriodThresholdView(
    var code: String,
    var label: String,
    var budgetText: String,
    var version: String,
    var titleText: String,
    var etaMinutes: String,
    var dockText: String,
    var localeText: String,
    var source: String,
    var statusLabel: String,
)

fun toDutyPeriodThresholdView(item: DutyPeriodThreshold): DutyPeriodThresholdView {
    return DutyPeriodThresholdView(
        code = item.code,
        label = item.label,
        budgetText = "%.2f".format(item.budget),
        version = item.version.toString(),
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        dockText = item.dock,
        localeText = truncate(item.locale, 120),
        source = truncate(item.source, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodThresholdViews(items: MutableList<DutyPeriodThreshold>): MutableList<DutyPeriodThresholdView> {
    val views = mutableListOf<DutyPeriodThresholdView>()
    for (item in items) {
        views.add(toDutyPeriodThresholdView(item))
    }
    return views
}

fun sumDutyPeriodThresholdView(view: DutyPeriodThresholdView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedBudget + parsedVersion.toDouble() + parsedEtaMinutes.toDouble()
}
