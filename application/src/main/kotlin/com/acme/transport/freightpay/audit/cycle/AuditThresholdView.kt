package com.acme.transport.freightpay.audit.cycle

/** How screens and exports show a audit threshold. */
data class AuditThresholdView(
    var codeText: String,
    var name: String,
    var score: String,
    var limit: String,
    var carrier: String,
    var vehicleId: String,
    var activeText: String,
    var rate: String,
    var statusLabel: String,
)

fun toAuditThresholdView(item: AuditThreshold): AuditThresholdView {
    return AuditThresholdView(
        codeText = item.code,
        name = item.name,
        score = "%.2f".format(item.score),
        limit = item.limit.toString(),
        carrier = item.carrier,
        vehicleId = item.vehicleId,
        activeText = "${item.active}",
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditThresholdViews(items: MutableList<AuditThreshold>): MutableList<AuditThresholdView> {
    val views = mutableListOf<AuditThresholdView>()
    for (item in items) {
        views.add(toAuditThresholdView(item))
    }
    return views
}

fun sumAuditThresholdView(view: AuditThresholdView): Double {
    val parsedScore = view.score.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedScore + parsedLimit.toDouble() + parsedRate
}
