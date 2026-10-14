package com.acme.transport.fleetops.inspection.inspectionpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global inspection policy. */
data class GlobalInspectionPolicyView(
    var codeText: String,
    var nameText: String,
    var category: String,
    var stopCountText: String,
    var sequence: String,
    var expiresAtText: String,
    var titleText: String,
    var statusLabel: String,
)

fun toGlobalInspectionPolicyView(item: GlobalInspectionPolicy): GlobalInspectionPolicyView {
    return GlobalInspectionPolicyView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        category = item.category,
        stopCountText = item.stopCount.toString(),
        sequence = item.sequence.toString(),
        expiresAtText = item.expiresAt.toString(),
        titleText = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalInspectionPolicyViews(items: MutableList<GlobalInspectionPolicy>): MutableList<GlobalInspectionPolicyView> {
    val views = mutableListOf<GlobalInspectionPolicyView>()
    for (item in items) {
        views.add(toGlobalInspectionPolicyView(item))
    }
    return views
}

fun sumGlobalInspectionPolicyView(view: GlobalInspectionPolicyView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedStopCount.toDouble() + parsedSequence.toDouble() + parsedExpiresAt.toDouble()
}
