package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending duty period preference. */
data class PendingDutyPeriodPreferenceView(
    var codeText: String,
    var labelText: String,
    var channelText: String,
    var phone: String,
    var revision: String,
    var dock: String,
    var statusLabel: String,
)

fun toPendingDutyPeriodPreferenceView(item: PendingDutyPeriodPreference): PendingDutyPeriodPreferenceView {
    return PendingDutyPeriodPreferenceView(
        codeText = item.code,
        labelText = truncate(item.label, 40),
        channelText = item.channel,
        phone = truncate(item.phone, 120),
        revision = item.revision.toString(),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDutyPeriodPreferenceViews(items: MutableList<PendingDutyPeriodPreference>): MutableList<PendingDutyPeriodPreferenceView> {
    val views = mutableListOf<PendingDutyPeriodPreferenceView>()
    for (item in items) {
        views.add(toPendingDutyPeriodPreferenceView(item))
    }
    return views
}

fun sumPendingDutyPeriodPreferenceView(view: PendingDutyPeriodPreferenceView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
