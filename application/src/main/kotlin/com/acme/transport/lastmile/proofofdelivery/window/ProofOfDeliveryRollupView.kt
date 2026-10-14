package com.acme.transport.lastmile.proofofdelivery.window

/** How screens and exports show a proof of delivery rollup. */
data class ProofOfDeliveryRollupView(
    var code: String,
    var title: String,
    var batchSizeText: String,
    var attempts: String,
    var carrier: String,
    var minimumText: String,
    var regionText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryRollupView(item: ProofOfDeliveryRollup): ProofOfDeliveryRollupView {
    return ProofOfDeliveryRollupView(
        code = item.code,
        title = item.title,
        batchSizeText = item.batchSize.toString(),
        attempts = item.attempts.toString(),
        carrier = item.carrier,
        minimumText = "%.2f".format(item.minimum),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryRollupViews(items: MutableList<ProofOfDeliveryRollup>): MutableList<ProofOfDeliveryRollupView> {
    val views = mutableListOf<ProofOfDeliveryRollupView>()
    for (item in items) {
        views.add(toProofOfDeliveryRollupView(item))
    }
    return views
}

fun sumProofOfDeliveryRollupView(view: ProofOfDeliveryRollupView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedBatchSize.toDouble() + parsedAttempts.toDouble() + parsedMinimum
}
