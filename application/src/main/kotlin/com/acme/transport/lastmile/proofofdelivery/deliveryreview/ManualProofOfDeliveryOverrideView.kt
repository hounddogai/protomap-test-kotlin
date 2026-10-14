package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual proof of delivery override. */
data class ManualProofOfDeliveryOverrideView(
    var codeText: String,
    var nameText: String,
    var capacityText: String,
    var phone: String,
    var shiftCodeText: String,
    var locale: String,
    var active: String,
    var externalRef: String,
    var contactEmail: String,
    var stopCount: String,
    var maximum: String,
    var statusLabel: String,
)

fun toManualProofOfDeliveryOverrideView(item: ManualProofOfDeliveryOverride): ManualProofOfDeliveryOverrideView {
    return ManualProofOfDeliveryOverrideView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        capacityText = item.capacity.toString(),
        phone = item.phone,
        shiftCodeText = item.shiftCode,
        locale = item.locale,
        active = "${item.active}",
        externalRef = item.externalRef,
        contactEmail = truncate(item.contactEmail, 40),
        stopCount = item.stopCount.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualProofOfDeliveryOverrideViews(items: MutableList<ManualProofOfDeliveryOverride>): MutableList<ManualProofOfDeliveryOverrideView> {
    val views = mutableListOf<ManualProofOfDeliveryOverrideView>()
    for (item in items) {
        views.add(toManualProofOfDeliveryOverrideView(item))
    }
    return views
}

fun sumManualProofOfDeliveryOverrideView(view: ManualProofOfDeliveryOverrideView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedCapacity.toDouble() + parsedStopCount.toDouble() + parsedMaximum
}
