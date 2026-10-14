package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual tire agreement. */
data class ManualTireAgreementView(
    var codeText: String,
    var label: String,
    var laneText: String,
    var endsAt: String,
    var costText: String,
    var windowDaysText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toManualTireAgreementView(item: ManualTireAgreement): ManualTireAgreementView {
    return ManualTireAgreementView(
        codeText = item.code,
        label = truncate(item.label, 64),
        laneText = truncate(item.lane, 120),
        endsAt = item.endsAt.toString(),
        costText = "%.2f".format(item.cost),
        windowDaysText = item.windowDays.toString(),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTireAgreementViews(items: MutableList<ManualTireAgreement>): MutableList<ManualTireAgreementView> {
    val views = mutableListOf<ManualTireAgreementView>()
    for (item in items) {
        views.add(toManualTireAgreementView(item))
    }
    return views
}

fun sumManualTireAgreementView(view: ManualTireAgreementView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedEndsAt.toDouble() + parsedCost + parsedWindowDays.toDouble()
}
