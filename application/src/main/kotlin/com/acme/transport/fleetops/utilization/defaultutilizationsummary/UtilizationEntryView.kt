package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization entry. */
data class UtilizationEntryView(
    var code: String,
    var title: String,
    var loadKg: String,
    var thresholdText: String,
    var windowDays: String,
    var expiresAt: String,
    var active: String,
    var sequence: String,
    var statusLabel: String,
)

fun toUtilizationEntryView(item: UtilizationEntry): UtilizationEntryView {
    return UtilizationEntryView(
        code = truncate(item.code, 64),
        title = item.title,
        loadKg = "%.2f".format(item.loadKg),
        thresholdText = "%.2f".format(item.threshold),
        windowDays = item.windowDays.toString(),
        expiresAt = item.expiresAt.toString(),
        active = "${item.active}",
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationEntryViews(items: MutableList<UtilizationEntry>): MutableList<UtilizationEntryView> {
    val views = mutableListOf<UtilizationEntryView>()
    for (item in items) {
        views.add(toUtilizationEntryView(item))
    }
    return views
}

fun sumUtilizationEntryView(view: UtilizationEntryView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedLoadKg + parsedThreshold + parsedWindowDays.toDouble()
}
