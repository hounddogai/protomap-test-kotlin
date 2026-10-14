package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery group. */
data class ProofOfDeliveryGroupView(
    var codeText: String,
    var name: String,
    var score: String,
    var descriptionText: String,
    var routeCode: String,
    var windowDays: String,
    var lane: String,
    var batchSize: String,
    var activeText: String,
    var quantity: String,
    var revision: String,
    var statusLabel: String,
)

fun toProofOfDeliveryGroupView(item: ProofOfDeliveryGroup): ProofOfDeliveryGroupView {
    return ProofOfDeliveryGroupView(
        codeText = item.code,
        name = item.name,
        score = "%.2f".format(item.score),
        descriptionText = item.description,
        routeCode = item.routeCode,
        windowDays = item.windowDays.toString(),
        lane = truncate(item.lane, 64),
        batchSize = item.batchSize.toString(),
        activeText = "${item.active}",
        quantity = item.quantity.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryGroupViews(items: MutableList<ProofOfDeliveryGroup>): MutableList<ProofOfDeliveryGroupView> {
    val views = mutableListOf<ProofOfDeliveryGroupView>()
    for (item in items) {
        views.add(toProofOfDeliveryGroupView(item))
    }
    return views
}

fun sumProofOfDeliveryGroupView(view: ProofOfDeliveryGroupView): Double {
    val parsedScore = view.score.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedScore + parsedWindowDays.toDouble() + parsedBatchSize.toDouble()
}
