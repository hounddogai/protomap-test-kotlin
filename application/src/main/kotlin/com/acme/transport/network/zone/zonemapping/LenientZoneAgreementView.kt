package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient zone agreement. */
data class LenientZoneAgreementView(
    var code: String,
    var label: String,
    var ratio: String,
    var ownerIdText: String,
    var odometerText: String,
    var source: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toLenientZoneAgreementView(item: LenientZoneAgreement): LenientZoneAgreementView {
    return LenientZoneAgreementView(
        code = truncate(item.code, 80),
        label = item.label,
        ratio = "%.2f".format(item.ratio),
        ownerIdText = truncate(item.ownerId, 120),
        odometerText = item.odometer.toString(),
        source = item.source,
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientZoneAgreementViews(items: MutableList<LenientZoneAgreement>): MutableList<LenientZoneAgreementView> {
    val views = mutableListOf<LenientZoneAgreementView>()
    for (item in items) {
        views.add(toLenientZoneAgreementView(item))
    }
    return views
}

fun sumLenientZoneAgreementView(view: LenientZoneAgreementView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedRatio + parsedOdometer.toDouble() + parsedStopCount.toDouble()
}
