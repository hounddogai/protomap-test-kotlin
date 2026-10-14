package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual duty period notice. */
data class ManualDutyPeriodNoticeView(
    var code: String,
    var label: String,
    var channel: String,
    var unitText: String,
    var depot: String,
    var locale: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toManualDutyPeriodNoticeView(item: ManualDutyPeriodNotice): ManualDutyPeriodNoticeView {
    return ManualDutyPeriodNoticeView(
        code = item.code,
        label = item.label,
        channel = item.channel,
        unitText = item.unit,
        depot = truncate(item.depot, 120),
        locale = item.locale,
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDutyPeriodNoticeViews(items: MutableList<ManualDutyPeriodNotice>): MutableList<ManualDutyPeriodNoticeView> {
    val views = mutableListOf<ManualDutyPeriodNoticeView>()
    for (item in items) {
        views.add(toManualDutyPeriodNoticeView(item))
    }
    return views
}

fun sumManualDutyPeriodNoticeView(view: ManualDutyPeriodNoticeView): Double {
    val parsedBudget = view.budgetText.toDouble()
    return parsedBudget
}
