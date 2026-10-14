package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global proof of delivery override. */
data class GlobalProofOfDeliveryOverrideView(
    var code: String,
    var nameText: String,
    var retries: String,
    var sequence: String,
    var score: String,
    var statusLabel: String,
)

fun toGlobalProofOfDeliveryOverrideView(item: GlobalProofOfDeliveryOverride): GlobalProofOfDeliveryOverrideView {
    return GlobalProofOfDeliveryOverrideView(
        code = truncate(item.code, 64),
        nameText = truncate(item.name, 120),
        retries = item.retries.toString(),
        sequence = item.sequence.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalProofOfDeliveryOverrideViews(items: MutableList<GlobalProofOfDeliveryOverride>): MutableList<GlobalProofOfDeliveryOverrideView> {
    val views = mutableListOf<GlobalProofOfDeliveryOverrideView>()
    for (item in items) {
        views.add(toGlobalProofOfDeliveryOverrideView(item))
    }
    return views
}

fun sumGlobalProofOfDeliveryOverrideView(view: GlobalProofOfDeliveryOverrideView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedScore = view.score.toDouble()
    return parsedRetries.toDouble() + parsedSequence.toDouble() + parsedScore
}
