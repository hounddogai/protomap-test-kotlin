package com.acme.transport.freightpay.audit.exception

/** How screens and exports show a strict audit schedule. */
data class StrictAuditScheduleView(
    var codeText: String,
    var label: String,
    var attempts: String,
    var retriesText: String,
    var ceilingText: String,
    var activeText: String,
    var fuelLiters: String,
    var phone: String,
    var notes: String,
    var priority: String,
    var statusLabel: String,
)

fun toStrictAuditScheduleView(item: StrictAuditSchedule): StrictAuditScheduleView {
    return StrictAuditScheduleView(
        codeText = item.code,
        label = item.label,
        attempts = item.attempts.toString(),
        retriesText = item.retries.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        activeText = "${item.active}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        phone = item.phone,
        notes = item.notes,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAuditScheduleViews(items: MutableList<StrictAuditSchedule>): MutableList<StrictAuditScheduleView> {
    val views = mutableListOf<StrictAuditScheduleView>()
    for (item in items) {
        views.add(toStrictAuditScheduleView(item))
    }
    return views
}

fun sumStrictAuditScheduleView(view: StrictAuditScheduleView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedAttempts.toDouble() + parsedRetries.toDouble() + parsedCeiling
}
