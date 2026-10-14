package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor audit. */
data class EmissionFactorAuditView(
    var code: String,
    var name: String,
    var localeText: String,
    var startsAtText: String,
    var contactEmailText: String,
    var discountText: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toEmissionFactorAuditView(item: EmissionFactorAudit): EmissionFactorAuditView {
    return EmissionFactorAuditView(
        code = item.code,
        name = truncate(item.name, 120),
        localeText = truncate(item.locale, 120),
        startsAtText = item.startsAt.toString(),
        contactEmailText = item.contactEmail,
        discountText = "%.2f".format(item.discount),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorAuditViews(items: MutableList<EmissionFactorAudit>): MutableList<EmissionFactorAuditView> {
    val views = mutableListOf<EmissionFactorAuditView>()
    for (item in items) {
        views.add(toEmissionFactorAuditView(item))
    }
    return views
}

fun sumEmissionFactorAuditView(view: EmissionFactorAuditView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedDiscount = view.discountText.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedStartsAt.toDouble() + parsedDiscount + parsedExpiresAt.toDouble()
}
