package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual duty period threshold. */
data class ManualDutyPeriodThresholdView(
    var codeText: String,
    var label: String,
    var distanceKm: String,
    var visible: String,
    var notes: String,
    var retries: String,
    var statusLabel: String,
)

fun toManualDutyPeriodThresholdView(item: ManualDutyPeriodThreshold): ManualDutyPeriodThresholdView {
    return ManualDutyPeriodThresholdView(
        codeText = truncate(item.code, 80),
        label = item.label,
        distanceKm = "%.2f".format(item.distanceKm),
        visible = "${item.visible}",
        notes = item.notes,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDutyPeriodThresholdViews(items: MutableList<ManualDutyPeriodThreshold>): MutableList<ManualDutyPeriodThresholdView> {
    val views = mutableListOf<ManualDutyPeriodThresholdView>()
    for (item in items) {
        views.add(toManualDutyPeriodThresholdView(item))
    }
    return views
}

fun sumManualDutyPeriodThresholdView(view: ManualDutyPeriodThresholdView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedDistanceKm + parsedRetries.toDouble()
}
