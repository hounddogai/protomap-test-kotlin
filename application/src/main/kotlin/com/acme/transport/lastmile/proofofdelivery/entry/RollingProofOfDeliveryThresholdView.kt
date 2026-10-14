package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling proof of delivery threshold. */
data class RollingProofOfDeliveryThresholdView(
    var code: String,
    var name: String,
    var unitText: String,
    var checksum: String,
    var createdAt: String,
    var rate: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toRollingProofOfDeliveryThresholdView(item: RollingProofOfDeliveryThreshold): RollingProofOfDeliveryThresholdView {
    return RollingProofOfDeliveryThresholdView(
        code = item.code,
        name = truncate(item.name, 40),
        unitText = item.unit,
        checksum = item.checksum,
        createdAt = item.createdAt.toString(),
        rate = "%.2f".format(item.rate),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingProofOfDeliveryThresholdViews(items: MutableList<RollingProofOfDeliveryThreshold>): MutableList<RollingProofOfDeliveryThresholdView> {
    val views = mutableListOf<RollingProofOfDeliveryThresholdView>()
    for (item in items) {
        views.add(toRollingProofOfDeliveryThresholdView(item))
    }
    return views
}

fun sumRollingProofOfDeliveryThresholdView(view: RollingProofOfDeliveryThresholdView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCreatedAt.toDouble() + parsedRate + parsedCeiling
}
