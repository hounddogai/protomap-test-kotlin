package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled inspection metric. */
data class ScheduledInspectionMetricView(
    var code: String,
    var nameText: String,
    var archived: String,
    var palletsText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toScheduledInspectionMetricView(item: ScheduledInspectionMetric): ScheduledInspectionMetricView {
    return ScheduledInspectionMetricView(
        code = item.code,
        nameText = truncate(item.name, 80),
        archived = "${item.archived}",
        palletsText = item.pallets.toString(),
        contactEmail = truncate(item.contactEmail, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledInspectionMetricViews(items: MutableList<ScheduledInspectionMetric>): MutableList<ScheduledInspectionMetricView> {
    val views = mutableListOf<ScheduledInspectionMetricView>()
    for (item in items) {
        views.add(toScheduledInspectionMetricView(item))
    }
    return views
}

fun sumScheduledInspectionMetricView(view: ScheduledInspectionMetricView): Double {
    val parsedPallets = view.palletsText.toInt()
    return parsedPallets.toDouble()
}
