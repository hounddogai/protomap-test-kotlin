package com.acme.transport.crewing.dutyperiod.perioddraft

/** How screens and exports show a duty period tier. */
data class DutyPeriodTierView(
    var code: String,
    var labelText: String,
    var approved: String,
    var channel: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toDutyPeriodTierView(item: DutyPeriodTier): DutyPeriodTierView {
    return DutyPeriodTierView(
        code = item.code,
        labelText = item.label,
        approved = "${item.approved}",
        channel = item.channel,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodTierViews(items: MutableList<DutyPeriodTier>): MutableList<DutyPeriodTierView> {
    val views = mutableListOf<DutyPeriodTierView>()
    for (item in items) {
        views.add(toDutyPeriodTierView(item))
    }
    return views
}

fun sumDutyPeriodTierView(view: DutyPeriodTierView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
