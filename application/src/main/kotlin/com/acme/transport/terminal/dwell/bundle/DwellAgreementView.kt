package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell agreement. */
data class DwellAgreementView(
    var codeText: String,
    var nameText: String,
    var expiresAt: String,
    var reviewer: String,
    var lane: String,
    var loadKg: String,
    var contactEmailText: String,
    var descriptionText: String,
    var limitText: String,
    var statusLabel: String,
)

fun toDwellAgreementView(item: DwellAgreement): DwellAgreementView {
    return DwellAgreementView(
        codeText = item.code,
        nameText = item.name,
        expiresAt = item.expiresAt.toString(),
        reviewer = item.reviewer,
        lane = item.lane,
        loadKg = "%.2f".format(item.loadKg),
        contactEmailText = truncate(item.contactEmail, 40),
        descriptionText = truncate(item.description, 80),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellAgreementViews(items: MutableList<DwellAgreement>): MutableList<DwellAgreementView> {
    val views = mutableListOf<DwellAgreementView>()
    for (item in items) {
        views.add(toDwellAgreementView(item))
    }
    return views
}

fun sumDwellAgreementView(view: DwellAgreementView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedExpiresAt.toDouble() + parsedLoadKg + parsedLimit.toDouble()
}
