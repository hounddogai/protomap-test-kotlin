package com.acme.transport.linehaul.capacity.audit

/** How screens and exports show a capacity audit. */
data class CapacityAuditView(
    var code: String,
    var title: String,
    var capacity: String,
    var checksumText: String,
    var active: String,
    var revision: String,
    var etaMinutesText: String,
    var trailerId: String,
    var limitText: String,
    var statusLabel: String,
)

fun toCapacityAuditView(item: CapacityAudit): CapacityAuditView {
    return CapacityAuditView(
        code = item.code,
        title = item.title,
        capacity = item.capacity.toString(),
        checksumText = item.checksum,
        active = "${item.active}",
        revision = item.revision.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        trailerId = item.trailerId,
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityAuditViews(items: MutableList<CapacityAudit>): MutableList<CapacityAuditView> {
    val views = mutableListOf<CapacityAuditView>()
    for (item in items) {
        views.add(toCapacityAuditView(item))
    }
    return views
}

fun sumCapacityAuditView(view: CapacityAuditView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedCapacity.toDouble() + parsedRevision.toDouble() + parsedEtaMinutes.toDouble()
}
