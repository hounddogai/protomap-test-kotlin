package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary ev charge agreement. */
data class PrimaryEvChargeAgreementView(
    var codeText: String,
    var name: String,
    var channel: String,
    var category: String,
    var description: String,
    var checksum: String,
    var amount: String,
    var statusLabel: String,
)

fun toPrimaryEvChargeAgreementView(item: PrimaryEvChargeAgreement): PrimaryEvChargeAgreementView {
    return PrimaryEvChargeAgreementView(
        codeText = item.code,
        name = item.name,
        channel = item.channel,
        category = item.category,
        description = truncate(item.description, 120),
        checksum = item.checksum,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryEvChargeAgreementViews(items: MutableList<PrimaryEvChargeAgreement>): MutableList<PrimaryEvChargeAgreementView> {
    val views = mutableListOf<PrimaryEvChargeAgreementView>()
    for (item in items) {
        views.add(toPrimaryEvChargeAgreementView(item))
    }
    return views
}

fun sumPrimaryEvChargeAgreementView(view: PrimaryEvChargeAgreementView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
