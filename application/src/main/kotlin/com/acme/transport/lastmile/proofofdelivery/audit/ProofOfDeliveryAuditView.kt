package com.acme.transport.lastmile.proofofdelivery.audit

/** How screens and exports show a proof of delivery audit. */
data class ProofOfDeliveryAuditView(
    var code: String,
    var labelText: String,
    var priorityText: String,
    var approvedText: String,
    var batchSizeText: String,
    var marginText: String,
    var endsAt: String,
    var distanceKm: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryAuditView(item: ProofOfDeliveryAudit): ProofOfDeliveryAuditView {
    return ProofOfDeliveryAuditView(
        code = item.code,
        labelText = item.label,
        priorityText = item.priority.toString(),
        approvedText = "${item.approved}",
        batchSizeText = item.batchSize.toString(),
        marginText = "%.2f".format(item.margin),
        endsAt = item.endsAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryAuditViews(items: MutableList<ProofOfDeliveryAudit>): MutableList<ProofOfDeliveryAuditView> {
    val views = mutableListOf<ProofOfDeliveryAuditView>()
    for (item in items) {
        views.add(toProofOfDeliveryAuditView(item))
    }
    return views
}

fun sumProofOfDeliveryAuditView(view: ProofOfDeliveryAuditView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedPriority.toDouble() + parsedBatchSize.toDouble() + parsedMargin
}
