package com.acme.transport.freightpay.audit.manifest

/** How screens and exports show a audit estimate. */
data class AuditEstimateView(
    var code: String,
    var title: String,
    var sequence: String,
    var routeCodeText: String,
    var timezone: String,
    var amount: String,
    var version: String,
    var lane: String,
    var rate: String,
    var activeText: String,
    var notesText: String,
    var statusLabel: String,
)

fun toAuditEstimateView(item: AuditEstimate): AuditEstimateView {
    return AuditEstimateView(
        code = item.code,
        title = item.title,
        sequence = item.sequence.toString(),
        routeCodeText = item.routeCode,
        timezone = item.timezone,
        amount = "%.2f".format(item.amount),
        version = item.version.toString(),
        lane = item.lane,
        rate = "%.2f".format(item.rate),
        activeText = "${item.active}",
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditEstimateViews(items: MutableList<AuditEstimate>): MutableList<AuditEstimateView> {
    val views = mutableListOf<AuditEstimateView>()
    for (item in items) {
        views.add(toAuditEstimateView(item))
    }
    return views
}

fun sumAuditEstimateView(view: AuditEstimateView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedSequence.toDouble() + parsedAmount + parsedVersion.toDouble()
}
