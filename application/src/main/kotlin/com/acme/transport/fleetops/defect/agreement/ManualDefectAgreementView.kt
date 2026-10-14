package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual defect agreement. */
data class ManualDefectAgreementView(
    var codeText: String,
    var title: String,
    var active: String,
    var locale: String,
    var visible: String,
    var currency: String,
    var channel: String,
    var timezone: String,
    var endsAt: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toManualDefectAgreementView(item: ManualDefectAgreement): ManualDefectAgreementView {
    return ManualDefectAgreementView(
        codeText = item.code,
        title = item.title,
        active = "${item.active}",
        locale = truncate(item.locale, 40),
        visible = "${item.visible}",
        currency = truncate(item.currency, 120),
        channel = truncate(item.channel, 80),
        timezone = item.timezone,
        endsAt = item.endsAt.toString(),
        reviewerText = truncate(item.reviewer, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDefectAgreementViews(items: MutableList<ManualDefectAgreement>): MutableList<ManualDefectAgreementView> {
    val views = mutableListOf<ManualDefectAgreementView>()
    for (item in items) {
        views.add(toManualDefectAgreementView(item))
    }
    return views
}

fun sumManualDefectAgreementView(view: ManualDefectAgreementView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEndsAt.toDouble()
}
