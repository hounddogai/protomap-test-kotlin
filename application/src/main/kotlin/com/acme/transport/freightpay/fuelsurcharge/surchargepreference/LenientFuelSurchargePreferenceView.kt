package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient fuel surcharge preference. */
data class LenientFuelSurchargePreferenceView(
    var code: String,
    var label: String,
    var depot: String,
    var versionText: String,
    var driverId: String,
    var batchSizeText: String,
    var currency: String,
    var statusLabel: String,
)

fun toLenientFuelSurchargePreferenceView(item: LenientFuelSurchargePreference): LenientFuelSurchargePreferenceView {
    return LenientFuelSurchargePreferenceView(
        code = item.code,
        label = item.label,
        depot = item.depot,
        versionText = item.version.toString(),
        driverId = truncate(item.driverId, 64),
        batchSizeText = item.batchSize.toString(),
        currency = truncate(item.currency, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFuelSurchargePreferenceViews(items: MutableList<LenientFuelSurchargePreference>): MutableList<LenientFuelSurchargePreferenceView> {
    val views = mutableListOf<LenientFuelSurchargePreferenceView>()
    for (item in items) {
        views.add(toLenientFuelSurchargePreferenceView(item))
    }
    return views
}

fun sumLenientFuelSurchargePreferenceView(view: LenientFuelSurchargePreferenceView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedVersion.toDouble() + parsedBatchSize.toDouble()
}
