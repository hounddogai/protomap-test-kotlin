package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance override. */
data class ClearanceOverrideView(
    var codeText: String,
    var name: String,
    var ratio: String,
    var ceilingText: String,
    var ownerIdText: String,
    var revisionText: String,
    var createdAt: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toClearanceOverrideView(item: ClearanceOverride): ClearanceOverrideView {
    return ClearanceOverrideView(
        codeText = item.code,
        name = truncate(item.name, 40),
        ratio = "%.2f".format(item.ratio),
        ceilingText = "%.2f".format(item.ceiling),
        ownerIdText = truncate(item.ownerId, 80),
        revisionText = item.revision.toString(),
        createdAt = item.createdAt.toString(),
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceOverrideViews(items: MutableList<ClearanceOverride>): MutableList<ClearanceOverrideView> {
    val views = mutableListOf<ClearanceOverrideView>()
    for (item in items) {
        views.add(toClearanceOverrideView(item))
    }
    return views
}

fun sumClearanceOverrideView(view: ClearanceOverrideView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedRatio + parsedCeiling + parsedRevision.toDouble()
}
