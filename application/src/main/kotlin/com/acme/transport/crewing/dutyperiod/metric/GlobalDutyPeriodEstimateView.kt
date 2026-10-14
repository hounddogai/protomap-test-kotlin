package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global duty period estimate. */
data class GlobalDutyPeriodEstimateView(
    var codeText: String,
    var title: String,
    var notes: String,
    var maximumText: String,
    var phone: String,
    var country: String,
    var unit: String,
    var statusLabel: String,
)

fun toGlobalDutyPeriodEstimateView(item: GlobalDutyPeriodEstimate): GlobalDutyPeriodEstimateView {
    return GlobalDutyPeriodEstimateView(
        codeText = truncate(item.code, 40),
        title = item.title,
        notes = item.notes,
        maximumText = "%.2f".format(item.maximum),
        phone = item.phone,
        country = item.country,
        unit = truncate(item.unit, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDutyPeriodEstimateViews(items: MutableList<GlobalDutyPeriodEstimate>): MutableList<GlobalDutyPeriodEstimateView> {
    val views = mutableListOf<GlobalDutyPeriodEstimateView>()
    for (item in items) {
        views.add(toGlobalDutyPeriodEstimateView(item))
    }
    return views
}

fun sumGlobalDutyPeriodEstimateView(view: GlobalDutyPeriodEstimateView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    return parsedMaximum
}
