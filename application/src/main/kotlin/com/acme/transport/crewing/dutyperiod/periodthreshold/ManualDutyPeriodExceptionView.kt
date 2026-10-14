package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual duty period exception. */
data class ManualDutyPeriodExceptionView(
    var code: String,
    var title: String,
    var contactEmail: String,
    var windowDaysText: String,
    var priority: String,
    var budget: String,
    var statusLabel: String,
)

fun toManualDutyPeriodExceptionView(item: ManualDutyPeriodException): ManualDutyPeriodExceptionView {
    return ManualDutyPeriodExceptionView(
        code = item.code,
        title = truncate(item.title, 40),
        contactEmail = truncate(item.contactEmail, 120),
        windowDaysText = item.windowDays.toString(),
        priority = item.priority.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDutyPeriodExceptionViews(items: MutableList<ManualDutyPeriodException>): MutableList<ManualDutyPeriodExceptionView> {
    val views = mutableListOf<ManualDutyPeriodExceptionView>()
    for (item in items) {
        views.add(toManualDutyPeriodExceptionView(item))
    }
    return views
}

fun sumManualDutyPeriodExceptionView(view: ManualDutyPeriodExceptionView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedWindowDays.toDouble() + parsedPriority.toDouble() + parsedBudget
}
