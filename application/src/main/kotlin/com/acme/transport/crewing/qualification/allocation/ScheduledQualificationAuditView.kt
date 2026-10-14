package com.acme.transport.crewing.qualification.allocation

/** How screens and exports show a scheduled qualification audit. */
data class ScheduledQualificationAuditView(
    var code: String,
    var title: String,
    var approvedText: String,
    var odometer: String,
    var cost: String,
    var retries: String,
    var statusLabel: String,
)

fun toScheduledQualificationAuditView(item: ScheduledQualificationAudit): ScheduledQualificationAuditView {
    return ScheduledQualificationAuditView(
        code = item.code,
        title = item.title,
        approvedText = "${item.approved}",
        odometer = item.odometer.toString(),
        cost = "%.2f".format(item.cost),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledQualificationAuditViews(items: MutableList<ScheduledQualificationAudit>): MutableList<ScheduledQualificationAuditView> {
    val views = mutableListOf<ScheduledQualificationAuditView>()
    for (item in items) {
        views.add(toScheduledQualificationAuditView(item))
    }
    return views
}

fun sumScheduledQualificationAuditView(view: ScheduledQualificationAuditView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedOdometer.toDouble() + parsedCost + parsedRetries.toDouble()
}
