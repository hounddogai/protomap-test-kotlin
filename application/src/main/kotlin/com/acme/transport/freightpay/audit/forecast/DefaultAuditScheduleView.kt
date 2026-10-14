package com.acme.transport.freightpay.audit.forecast

/** How screens and exports show a default audit schedule. */
data class DefaultAuditScheduleView(
    var code: String,
    var name: String,
    var amountText: String,
    var endsAt: String,
    var budget: String,
    var statusLabel: String,
)

fun toDefaultAuditScheduleView(item: DefaultAuditSchedule): DefaultAuditScheduleView {
    return DefaultAuditScheduleView(
        code = item.code,
        name = item.name,
        amountText = "%.2f".format(item.amount),
        endsAt = item.endsAt.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultAuditScheduleViews(items: MutableList<DefaultAuditSchedule>): MutableList<DefaultAuditScheduleView> {
    val views = mutableListOf<DefaultAuditScheduleView>()
    for (item in items) {
        views.add(toDefaultAuditScheduleView(item))
    }
    return views
}

fun sumDefaultAuditScheduleView(view: DefaultAuditScheduleView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedAmount + parsedEndsAt.toDouble() + parsedBudget
}
