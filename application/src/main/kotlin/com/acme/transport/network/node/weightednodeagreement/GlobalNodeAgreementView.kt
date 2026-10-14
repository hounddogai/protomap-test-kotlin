package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global node agreement. */
data class GlobalNodeAgreementView(
    var code: String,
    var name: String,
    var segment: String,
    var category: String,
    var phone: String,
    var timezoneText: String,
    var etaMinutesText: String,
    var contactEmail: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toGlobalNodeAgreementView(item: GlobalNodeAgreement): GlobalNodeAgreementView {
    return GlobalNodeAgreementView(
        code = truncate(item.code, 120),
        name = item.name,
        segment = truncate(item.segment, 120),
        category = item.category,
        phone = item.phone,
        timezoneText = item.timezone,
        etaMinutesText = item.etaMinutes.toString(),
        contactEmail = truncate(item.contactEmail, 40),
        reviewer = truncate(item.reviewer, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalNodeAgreementViews(items: MutableList<GlobalNodeAgreement>): MutableList<GlobalNodeAgreementView> {
    val views = mutableListOf<GlobalNodeAgreementView>()
    for (item in items) {
        views.add(toGlobalNodeAgreementView(item))
    }
    return views
}

fun sumGlobalNodeAgreementView(view: GlobalNodeAgreementView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedEtaMinutes.toDouble()
}
