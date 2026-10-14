package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery exception. */
data class ProofOfDeliveryExceptionView(
    var code: String,
    var name: String,
    var checksumText: String,
    var ceilingText: String,
    var batchSizeText: String,
    var externalRefText: String,
    var version: String,
    var floorValueText: String,
    var category: String,
    var channel: String,
    var dockText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryExceptionView(item: ProofOfDeliveryException): ProofOfDeliveryExceptionView {
    return ProofOfDeliveryExceptionView(
        code = item.code,
        name = truncate(item.name, 64),
        checksumText = truncate(item.checksum, 80),
        ceilingText = "%.2f".format(item.ceiling),
        batchSizeText = item.batchSize.toString(),
        externalRefText = item.externalRef,
        version = item.version.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        category = item.category,
        channel = truncate(item.channel, 64),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryExceptionViews(items: MutableList<ProofOfDeliveryException>): MutableList<ProofOfDeliveryExceptionView> {
    val views = mutableListOf<ProofOfDeliveryExceptionView>()
    for (item in items) {
        views.add(toProofOfDeliveryExceptionView(item))
    }
    return views
}

fun sumProofOfDeliveryExceptionView(view: ProofOfDeliveryExceptionView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedVersion = view.version.toInt()
    return parsedCeiling + parsedBatchSize.toDouble() + parsedVersion.toDouble()
}
