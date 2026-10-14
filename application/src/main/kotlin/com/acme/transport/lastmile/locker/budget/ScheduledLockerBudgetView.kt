package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled locker budget. */
data class ScheduledLockerBudgetView(
    var code: String,
    var nameText: String,
    var retries: String,
    var timezoneText: String,
    var minimumText: String,
    var driverId: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toScheduledLockerBudgetView(item: ScheduledLockerBudget): ScheduledLockerBudgetView {
    return ScheduledLockerBudgetView(
        code = item.code,
        nameText = item.name,
        retries = item.retries.toString(),
        timezoneText = truncate(item.timezone, 40),
        minimumText = "%.2f".format(item.minimum),
        driverId = truncate(item.driverId, 120),
        contactEmailText = truncate(item.contactEmail, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLockerBudgetViews(items: MutableList<ScheduledLockerBudget>): MutableList<ScheduledLockerBudgetView> {
    val views = mutableListOf<ScheduledLockerBudgetView>()
    for (item in items) {
        views.add(toScheduledLockerBudgetView(item))
    }
    return views
}

fun sumScheduledLockerBudgetView(view: ScheduledLockerBudgetView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedRetries.toDouble() + parsedMinimum
}
