package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break agreement. */
data class RestBreakAgreementView(
    var codeText: String,
    var title: String,
    var checksumText: String,
    var etaMinutes: String,
    var archived: String,
    var regionText: String,
    var statusLabel: String,
)

fun toRestBreakAgreementView(item: RestBreakAgreement): RestBreakAgreementView {
    return RestBreakAgreementView(
        codeText = item.code,
        title = truncate(item.title, 120),
        checksumText = truncate(item.checksum, 64),
        etaMinutes = item.etaMinutes.toString(),
        archived = "${item.archived}",
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakAgreementViews(items: MutableList<RestBreakAgreement>): MutableList<RestBreakAgreementView> {
    val views = mutableListOf<RestBreakAgreementView>()
    for (item in items) {
        views.add(toRestBreakAgreementView(item))
    }
    return views
}

fun sumRestBreakAgreementView(view: RestBreakAgreementView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
