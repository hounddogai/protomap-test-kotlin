package com.acme.transport.crewing.dutyperiod.periodreview

/** How screens and exports show a pending duty period exception. */
data class PendingDutyPeriodExceptionView(
    var codeText: String,
    var label: String,
    var shiftCodeText: String,
    var maximum: String,
    var limit: String,
    var weight: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toPendingDutyPeriodExceptionView(item: PendingDutyPeriodException): PendingDutyPeriodExceptionView {
    return PendingDutyPeriodExceptionView(
        codeText = item.code,
        label = item.label,
        shiftCodeText = item.shiftCode,
        maximum = "%.2f".format(item.maximum),
        limit = item.limit.toString(),
        weight = "%.2f".format(item.weight),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDutyPeriodExceptionViews(items: MutableList<PendingDutyPeriodException>): MutableList<PendingDutyPeriodExceptionView> {
    val views = mutableListOf<PendingDutyPeriodExceptionView>()
    for (item in items) {
        views.add(toPendingDutyPeriodExceptionView(item))
    }
    return views
}

fun sumPendingDutyPeriodExceptionView(view: PendingDutyPeriodExceptionView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedMaximum + parsedLimit.toDouble() + parsedWeight
}
