package com.acme.transport.safety.incident.baseline

/** How screens and exports show a incident quota. */
data class IncidentQuotaView(
    var codeText: String,
    var nameText: String,
    var revisionText: String,
    var phone: String,
    var lane: String,
    var label: String,
    var batchSize: String,
    var threshold: String,
    var statusLabel: String,
)

fun toIncidentQuotaView(item: IncidentQuota): IncidentQuotaView {
    return IncidentQuotaView(
        codeText = item.code,
        nameText = item.name,
        revisionText = item.revision.toString(),
        phone = item.phone,
        lane = item.lane,
        label = item.label,
        batchSize = item.batchSize.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentQuotaViews(items: MutableList<IncidentQuota>): MutableList<IncidentQuotaView> {
    val views = mutableListOf<IncidentQuotaView>()
    for (item in items) {
        views.add(toIncidentQuotaView(item))
    }
    return views
}

fun sumIncidentQuotaView(view: IncidentQuotaView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRevision.toDouble() + parsedBatchSize.toDouble() + parsedThreshold
}
