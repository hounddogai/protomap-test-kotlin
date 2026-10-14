package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery slot. */
data class ProofOfDeliverySlotView(
    var code: String,
    var labelText: String,
    var title: String,
    var routeCode: String,
    var name: String,
    var region: String,
    var floorValue: String,
    var quantity: String,
    var currency: String,
    var attempts: String,
    var carrier: String,
    var statusLabel: String,
)

fun toProofOfDeliverySlotView(item: ProofOfDeliverySlot): ProofOfDeliverySlotView {
    return ProofOfDeliverySlotView(
        code = item.code,
        labelText = item.label,
        title = truncate(item.title, 64),
        routeCode = item.routeCode,
        name = item.name,
        region = item.region,
        floorValue = "%.2f".format(item.floorValue),
        quantity = item.quantity.toString(),
        currency = truncate(item.currency, 64),
        attempts = item.attempts.toString(),
        carrier = truncate(item.carrier, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliverySlotViews(items: MutableList<ProofOfDeliverySlot>): MutableList<ProofOfDeliverySlotView> {
    val views = mutableListOf<ProofOfDeliverySlotView>()
    for (item in items) {
        views.add(toProofOfDeliverySlotView(item))
    }
    return views
}

fun sumProofOfDeliverySlotView(view: ProofOfDeliverySlotView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedFloorValue + parsedQuantity.toDouble() + parsedAttempts.toDouble()
}
