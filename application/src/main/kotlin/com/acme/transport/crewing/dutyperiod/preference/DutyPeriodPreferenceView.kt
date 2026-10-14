package com.acme.transport.crewing.dutyperiod.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period preference. */
data class DutyPeriodPreferenceView(
    var codeText: String,
    var title: String,
    var amount: String,
    var currencyText: String,
    var visible: String,
    var reviewerText: String,
    var endsAt: String,
    var distanceKm: String,
    var floorValue: String,
    var approved: String,
    var statusLabel: String,
)

fun toDutyPeriodPreferenceView(item: DutyPeriodPreference): DutyPeriodPreferenceView {
    return DutyPeriodPreferenceView(
        codeText = item.code,
        title = item.title,
        amount = "%.2f".format(item.amount),
        currencyText = item.currency,
        visible = "${item.visible}",
        reviewerText = truncate(item.reviewer, 80),
        endsAt = item.endsAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        floorValue = "%.2f".format(item.floorValue),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodPreferenceViews(items: MutableList<DutyPeriodPreference>): MutableList<DutyPeriodPreferenceView> {
    val views = mutableListOf<DutyPeriodPreferenceView>()
    for (item in items) {
        views.add(toDutyPeriodPreferenceView(item))
    }
    return views
}

fun sumDutyPeriodPreferenceView(view: DutyPeriodPreferenceView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedAmount + parsedEndsAt.toDouble() + parsedDistanceKm
}
