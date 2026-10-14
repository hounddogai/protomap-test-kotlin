package com.acme.transport.freightpay.accessorial.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled accessorial budget. */
data class ScheduledAccessorialBudgetView(
    var codeText: String,
    var title: String,
    var carrier: String,
    var limit: String,
    var graceDaysText: String,
    var countryText: String,
    var driverIdText: String,
    var vehicleId: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toScheduledAccessorialBudgetView(item: ScheduledAccessorialBudget): ScheduledAccessorialBudgetView {
    return ScheduledAccessorialBudgetView(
        codeText = item.code,
        title = truncate(item.title, 80),
        carrier = item.carrier,
        limit = item.limit.toString(),
        graceDaysText = item.graceDays.toString(),
        countryText = truncate(item.country, 40),
        driverIdText = truncate(item.driverId, 64),
        vehicleId = item.vehicleId,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledAccessorialBudgetViews(items: MutableList<ScheduledAccessorialBudget>): MutableList<ScheduledAccessorialBudgetView> {
    val views = mutableListOf<ScheduledAccessorialBudgetView>()
    for (item in items) {
        views.add(toScheduledAccessorialBudgetView(item))
    }
    return views
}

fun sumScheduledAccessorialBudgetView(view: ScheduledAccessorialBudgetView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedLimit.toDouble() + parsedGraceDays.toDouble() + parsedExpiresAt.toDouble()
}
