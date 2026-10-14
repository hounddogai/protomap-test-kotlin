package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive duty period checkpoint. */
data class AdaptiveDutyPeriodCheckpointView(
    var codeText: String,
    var title: String,
    var region: String,
    var dock: String,
    var graceDays: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toAdaptiveDutyPeriodCheckpointView(item: AdaptiveDutyPeriodCheckpoint): AdaptiveDutyPeriodCheckpointView {
    return AdaptiveDutyPeriodCheckpointView(
        codeText = truncate(item.code, 64),
        title = item.title,
        region = item.region,
        dock = item.dock,
        graceDays = item.graceDays.toString(),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDutyPeriodCheckpointViews(items: MutableList<AdaptiveDutyPeriodCheckpoint>): MutableList<AdaptiveDutyPeriodCheckpointView> {
    val views = mutableListOf<AdaptiveDutyPeriodCheckpointView>()
    for (item in items) {
        views.add(toAdaptiveDutyPeriodCheckpointView(item))
    }
    return views
}

fun sumAdaptiveDutyPeriodCheckpointView(view: AdaptiveDutyPeriodCheckpointView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedGraceDays.toDouble() + parsedRetries.toDouble()
}
