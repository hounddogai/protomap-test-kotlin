package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period agreement. */
data class DutyPeriodAgreementView(
    var code: String,
    var label: String,
    var channel: String,
    var visible: String,
    var driverIdText: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toDutyPeriodAgreementView(item: DutyPeriodAgreement): DutyPeriodAgreementView {
    return DutyPeriodAgreementView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 80),
        channel = truncate(item.channel, 64),
        visible = "${item.visible}",
        driverIdText = truncate(item.driverId, 40),
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodAgreementViews(items: MutableList<DutyPeriodAgreement>): MutableList<DutyPeriodAgreementView> {
    val views = mutableListOf<DutyPeriodAgreementView>()
    for (item in items) {
        views.add(toDutyPeriodAgreementView(item))
    }
    return views
}

fun sumDutyPeriodAgreementView(view: DutyPeriodAgreementView): Double {
    val parsedCapacity = view.capacityText.toInt()
    return parsedCapacity.toDouble()
}
