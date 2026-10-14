package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive tender agreement. */
data class AdaptiveTenderAgreementView(
    var code: String,
    var titleText: String,
    var pallets: String,
    var label: String,
    var shiftCodeText: String,
    var routeCode: String,
    var distanceKmText: String,
    var description: String,
    var ratio: String,
    var batchSize: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toAdaptiveTenderAgreementView(item: AdaptiveTenderAgreement): AdaptiveTenderAgreementView {
    return AdaptiveTenderAgreementView(
        code = item.code,
        titleText = item.title,
        pallets = item.pallets.toString(),
        label = truncate(item.label, 80),
        shiftCodeText = item.shiftCode,
        routeCode = item.routeCode,
        distanceKmText = "%.2f".format(item.distanceKm),
        description = truncate(item.description, 64),
        ratio = "%.2f".format(item.ratio),
        batchSize = item.batchSize.toString(),
        trailerId = truncate(item.trailerId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTenderAgreementViews(items: MutableList<AdaptiveTenderAgreement>): MutableList<AdaptiveTenderAgreementView> {
    val views = mutableListOf<AdaptiveTenderAgreementView>()
    for (item in items) {
        views.add(toAdaptiveTenderAgreementView(item))
    }
    return views
}

fun sumAdaptiveTenderAgreementView(view: AdaptiveTenderAgreementView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedPallets.toDouble() + parsedDistanceKm + parsedRatio
}
