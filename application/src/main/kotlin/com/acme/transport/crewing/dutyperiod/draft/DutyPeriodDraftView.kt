package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period draft. */
data class DutyPeriodDraftView(
    var code: String,
    var title: String,
    var externalRef: String,
    var label: String,
    var graceDays: String,
    var driverId: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toDutyPeriodDraftView(item: DutyPeriodDraft): DutyPeriodDraftView {
    return DutyPeriodDraftView(
        code = truncate(item.code, 64),
        title = item.title,
        externalRef = item.externalRef,
        label = item.label,
        graceDays = item.graceDays.toString(),
        driverId = item.driverId,
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodDraftViews(items: MutableList<DutyPeriodDraft>): MutableList<DutyPeriodDraftView> {
    val views = mutableListOf<DutyPeriodDraftView>()
    for (item in items) {
        views.add(toDutyPeriodDraftView(item))
    }
    return views
}

fun sumDutyPeriodDraftView(view: DutyPeriodDraftView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedGraceDays.toDouble() + parsedEndsAt.toDouble()
}
