package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted audit segment. */
data class WeightedAuditSegmentView(
    var code: String,
    var title: String,
    var vehicleId: String,
    var nameText: String,
    var capacity: String,
    var createdAt: String,
    var visible: String,
    var revision: String,
    var timezone: String,
    var statusLabel: String,
)

fun toWeightedAuditSegmentView(item: WeightedAuditSegment): WeightedAuditSegmentView {
    return WeightedAuditSegmentView(
        code = item.code,
        title = truncate(item.title, 80),
        vehicleId = truncate(item.vehicleId, 120),
        nameText = item.name,
        capacity = item.capacity.toString(),
        createdAt = item.createdAt.toString(),
        visible = "${item.visible}",
        revision = item.revision.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAuditSegmentViews(items: MutableList<WeightedAuditSegment>): MutableList<WeightedAuditSegmentView> {
    val views = mutableListOf<WeightedAuditSegmentView>()
    for (item in items) {
        views.add(toWeightedAuditSegmentView(item))
    }
    return views
}

fun sumWeightedAuditSegmentView(view: WeightedAuditSegmentView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedCapacity.toDouble() + parsedCreatedAt.toDouble() + parsedRevision.toDouble()
}
