package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global fuel surcharge threshold. */
data class GlobalFuelSurchargeThresholdView(
    var code: String,
    var titleText: String,
    var contactEmail: String,
    var shiftCode: String,
    var versionText: String,
    var segment: String,
    var quantity: String,
    var batchSize: String,
    var vehicleId: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toGlobalFuelSurchargeThresholdView(item: GlobalFuelSurchargeThreshold): GlobalFuelSurchargeThresholdView {
    return GlobalFuelSurchargeThresholdView(
        code = truncate(item.code, 80),
        titleText = item.title,
        contactEmail = item.contactEmail,
        shiftCode = item.shiftCode,
        versionText = item.version.toString(),
        segment = item.segment,
        quantity = item.quantity.toString(),
        batchSize = item.batchSize.toString(),
        vehicleId = item.vehicleId,
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFuelSurchargeThresholdViews(items: MutableList<GlobalFuelSurchargeThreshold>): MutableList<GlobalFuelSurchargeThresholdView> {
    val views = mutableListOf<GlobalFuelSurchargeThresholdView>()
    for (item in items) {
        views.add(toGlobalFuelSurchargeThresholdView(item))
    }
    return views
}

fun sumGlobalFuelSurchargeThresholdView(view: GlobalFuelSurchargeThresholdView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedVersion.toDouble() + parsedQuantity.toDouble() + parsedBatchSize.toDouble()
}
