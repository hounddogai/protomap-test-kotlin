package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection policy. */
data class InspectionPolicyView(
    var code: String,
    var title: String,
    var lane: String,
    var approved: String,
    var expiresAt: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toInspectionPolicyView(item: InspectionPolicy): InspectionPolicyView {
    return InspectionPolicyView(
        code = item.code,
        title = truncate(item.title, 40),
        lane = item.lane,
        approved = "${item.approved}",
        expiresAt = item.expiresAt.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionPolicyViews(items: MutableList<InspectionPolicy>): MutableList<InspectionPolicyView> {
    val views = mutableListOf<InspectionPolicyView>()
    for (item in items) {
        views.add(toInspectionPolicyView(item))
    }
    return views
}

fun sumInspectionPolicyView(view: InspectionPolicyView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedExpiresAt.toDouble()
}
