package com.acme.transport.crewing.dutyperiod.agreement

/** How screens and exports show a duty period template. */
data class DutyPeriodTemplateView(
    var code: String,
    var label: String,
    var limitText: String,
    var endsAt: String,
    var rate: String,
    var dock: String,
    var statusLabel: String,
)

fun toDutyPeriodTemplateView(item: DutyPeriodTemplate): DutyPeriodTemplateView {
    return DutyPeriodTemplateView(
        code = item.code,
        label = item.label,
        limitText = item.limit.toString(),
        endsAt = item.endsAt.toString(),
        rate = "%.2f".format(item.rate),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodTemplateViews(items: MutableList<DutyPeriodTemplate>): MutableList<DutyPeriodTemplateView> {
    val views = mutableListOf<DutyPeriodTemplateView>()
    for (item in items) {
        views.add(toDutyPeriodTemplateView(item))
    }
    return views
}

fun sumDutyPeriodTemplateView(view: DutyPeriodTemplateView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedLimit.toDouble() + parsedEndsAt.toDouble() + parsedRate
}
