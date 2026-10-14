package com.acme.transport.freightpay.dispute.adjustment

/** How screens and exports show a dispute audit. */
data class DisputeAuditView(
    var codeText: String,
    var nameText: String,
    var sequence: String,
    var minimum: String,
    var distanceKmText: String,
    var discount: String,
    var statusLabel: String,
)

fun toDisputeAuditView(item: DisputeAudit): DisputeAuditView {
    return DisputeAuditView(
        codeText = item.code,
        nameText = item.name,
        sequence = item.sequence.toString(),
        minimum = "%.2f".format(item.minimum),
        distanceKmText = "%.2f".format(item.distanceKm),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeAuditViews(items: MutableList<DisputeAudit>): MutableList<DisputeAuditView> {
    val views = mutableListOf<DisputeAuditView>()
    for (item in items) {
        views.add(toDisputeAuditView(item))
    }
    return views
}

fun sumDisputeAuditView(view: DisputeAuditView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedSequence.toDouble() + parsedMinimum + parsedDistanceKm
}
