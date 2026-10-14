package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive locker agreement. */
data class AdaptiveLockerAgreementView(
    var codeText: String,
    var name: String,
    var palletsText: String,
    var checksumText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toAdaptiveLockerAgreementView(item: AdaptiveLockerAgreement): AdaptiveLockerAgreementView {
    return AdaptiveLockerAgreementView(
        codeText = item.code,
        name = truncate(item.name, 80),
        palletsText = item.pallets.toString(),
        checksumText = item.checksum,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLockerAgreementViews(items: MutableList<AdaptiveLockerAgreement>): MutableList<AdaptiveLockerAgreementView> {
    val views = mutableListOf<AdaptiveLockerAgreementView>()
    for (item in items) {
        views.add(toAdaptiveLockerAgreementView(item))
    }
    return views
}

fun sumAdaptiveLockerAgreementView(view: AdaptiveLockerAgreementView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedPallets.toDouble() + parsedDistanceKm
}
