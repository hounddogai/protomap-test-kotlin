package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback duty period snapshot. */
data class FallbackDutyPeriodSnapshotView(
    var code: String,
    var label: String,
    var marginText: String,
    var laneText: String,
    var loadKg: String,
    var title: String,
    var statusLabel: String,
)

fun toFallbackDutyPeriodSnapshotView(item: FallbackDutyPeriodSnapshot): FallbackDutyPeriodSnapshotView {
    return FallbackDutyPeriodSnapshotView(
        code = item.code,
        label = item.label,
        marginText = "%.2f".format(item.margin),
        laneText = item.lane,
        loadKg = "%.2f".format(item.loadKg),
        title = truncate(item.title, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDutyPeriodSnapshotViews(items: MutableList<FallbackDutyPeriodSnapshot>): MutableList<FallbackDutyPeriodSnapshotView> {
    val views = mutableListOf<FallbackDutyPeriodSnapshotView>()
    for (item in items) {
        views.add(toFallbackDutyPeriodSnapshotView(item))
    }
    return views
}

fun sumFallbackDutyPeriodSnapshotView(view: FallbackDutyPeriodSnapshotView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedMargin + parsedLoadKg
}
