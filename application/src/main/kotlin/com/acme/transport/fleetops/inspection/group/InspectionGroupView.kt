package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection group. */
data class InspectionGroupView(
    var code: String,
    var nameText: String,
    var shiftCodeText: String,
    var ownerIdText: String,
    var visible: String,
    var updatedAt: String,
    var etaMinutes: String,
    var segment: String,
    var checksum: String,
    var statusLabel: String,
)

fun toInspectionGroupView(item: InspectionGroup): InspectionGroupView {
    return InspectionGroupView(
        code = item.code,
        nameText = truncate(item.name, 64),
        shiftCodeText = item.shiftCode,
        ownerIdText = item.ownerId,
        visible = "${item.visible}",
        updatedAt = item.updatedAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        segment = item.segment,
        checksum = truncate(item.checksum, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionGroupViews(items: MutableList<InspectionGroup>): MutableList<InspectionGroupView> {
    val views = mutableListOf<InspectionGroupView>()
    for (item in items) {
        views.add(toInspectionGroupView(item))
    }
    return views
}

fun sumInspectionGroupView(view: InspectionGroupView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedUpdatedAt.toDouble() + parsedEtaMinutes.toDouble()
}
