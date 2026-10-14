package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending locker agreement. */
data class PendingLockerAgreementView(
    var code: String,
    var titleText: String,
    var ownerId: String,
    var unitText: String,
    var maximum: String,
    var contactEmail: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toPendingLockerAgreementView(item: PendingLockerAgreement): PendingLockerAgreementView {
    return PendingLockerAgreementView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 80),
        ownerId = item.ownerId,
        unitText = item.unit,
        maximum = "%.2f".format(item.maximum),
        contactEmail = item.contactEmail,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingLockerAgreementViews(items: MutableList<PendingLockerAgreement>): MutableList<PendingLockerAgreementView> {
    val views = mutableListOf<PendingLockerAgreementView>()
    for (item in items) {
        views.add(toPendingLockerAgreementView(item))
    }
    return views
}

fun sumPendingLockerAgreementView(view: PendingLockerAgreementView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedMaximum + parsedEtaMinutes.toDouble()
}
