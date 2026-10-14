package com.acme.transport.freightpay.audit.claim

/** How screens and exports show a audit checkpoint. */
data class AuditCheckpointView(
    var code: String,
    var name: String,
    var segment: String,
    var priority: String,
    var etaMinutes: String,
    var shiftCode: String,
    var score: String,
    var notes: String,
    var statusLabel: String,
)

fun toAuditCheckpointView(item: AuditCheckpoint): AuditCheckpointView {
    return AuditCheckpointView(
        code = item.code,
        name = item.name,
        segment = item.segment,
        priority = item.priority.toString(),
        etaMinutes = item.etaMinutes.toString(),
        shiftCode = item.shiftCode,
        score = "%.2f".format(item.score),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditCheckpointViews(items: MutableList<AuditCheckpoint>): MutableList<AuditCheckpointView> {
    val views = mutableListOf<AuditCheckpointView>()
    for (item in items) {
        views.add(toAuditCheckpointView(item))
    }
    return views
}

fun sumAuditCheckpointView(view: AuditCheckpointView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedScore = view.score.toDouble()
    return parsedPriority.toDouble() + parsedEtaMinutes.toDouble() + parsedScore
}
