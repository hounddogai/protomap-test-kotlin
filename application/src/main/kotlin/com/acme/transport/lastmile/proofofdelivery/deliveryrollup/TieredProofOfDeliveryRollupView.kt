package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered proof of delivery rollup. */
data class TieredProofOfDeliveryRollupView(
    var code: String,
    var name: String,
    var attempts: String,
    var createdAt: String,
    var driverIdText: String,
    var floorValueText: String,
    var regionText: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toTieredProofOfDeliveryRollupView(item: TieredProofOfDeliveryRollup): TieredProofOfDeliveryRollupView {
    return TieredProofOfDeliveryRollupView(
        code = item.code,
        name = item.name,
        attempts = item.attempts.toString(),
        createdAt = item.createdAt.toString(),
        driverIdText = truncate(item.driverId, 120),
        floorValueText = "%.2f".format(item.floorValue),
        regionText = item.region,
        reviewerText = truncate(item.reviewer, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredProofOfDeliveryRollupViews(items: MutableList<TieredProofOfDeliveryRollup>): MutableList<TieredProofOfDeliveryRollupView> {
    val views = mutableListOf<TieredProofOfDeliveryRollupView>()
    for (item in items) {
        views.add(toTieredProofOfDeliveryRollupView(item))
    }
    return views
}

fun sumTieredProofOfDeliveryRollupView(view: TieredProofOfDeliveryRollupView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedAttempts.toDouble() + parsedCreatedAt.toDouble() + parsedFloorValue
}
