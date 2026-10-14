package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped duty period policy. */
data class CappedDutyPeriodPolicyView(
    var code: String,
    var label: String,
    var priorityText: String,
    var odometer: String,
    var contactEmailText: String,
    var endsAt: String,
    var attempts: String,
    var palletsText: String,
    var channelText: String,
    var checksum: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toCappedDutyPeriodPolicyView(item: CappedDutyPeriodPolicy): CappedDutyPeriodPolicyView {
    return CappedDutyPeriodPolicyView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 80),
        priorityText = item.priority.toString(),
        odometer = item.odometer.toString(),
        contactEmailText = truncate(item.contactEmail, 40),
        endsAt = item.endsAt.toString(),
        attempts = item.attempts.toString(),
        palletsText = item.pallets.toString(),
        channelText = item.channel,
        checksum = item.checksum,
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDutyPeriodPolicyViews(items: MutableList<CappedDutyPeriodPolicy>): MutableList<CappedDutyPeriodPolicyView> {
    val views = mutableListOf<CappedDutyPeriodPolicyView>()
    for (item in items) {
        views.add(toCappedDutyPeriodPolicyView(item))
    }
    return views
}

fun sumCappedDutyPeriodPolicyView(view: CappedDutyPeriodPolicyView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedPriority.toDouble() + parsedOdometer.toDouble() + parsedEndsAt.toDouble()
}
