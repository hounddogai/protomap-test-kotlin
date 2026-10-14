package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy locker agreement. */
data class LegacyLockerAgreementView(
    var code: String,
    var title: String,
    var palletsText: String,
    var vehicleId: String,
    var description: String,
    var label: String,
    var currency: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toLegacyLockerAgreementView(item: LegacyLockerAgreement): LegacyLockerAgreementView {
    return LegacyLockerAgreementView(
        code = item.code,
        title = item.title,
        palletsText = item.pallets.toString(),
        vehicleId = item.vehicleId,
        description = truncate(item.description, 80),
        label = item.label,
        currency = item.currency,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLockerAgreementViews(items: MutableList<LegacyLockerAgreement>): MutableList<LegacyLockerAgreementView> {
    val views = mutableListOf<LegacyLockerAgreementView>()
    for (item in items) {
        views.add(toLegacyLockerAgreementView(item))
    }
    return views
}

fun sumLegacyLockerAgreementView(view: LegacyLockerAgreementView): Double {
    val parsedPallets = view.palletsText.toInt()
    return parsedPallets.toDouble()
}
