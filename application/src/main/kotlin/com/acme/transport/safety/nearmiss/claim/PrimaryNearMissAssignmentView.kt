package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary near miss assignment. */
data class PrimaryNearMissAssignmentView(
    var code: String,
    var labelText: String,
    var phoneText: String,
    var limitText: String,
    var carrierText: String,
    var currency: String,
    var source: String,
    var description: String,
    var statusLabel: String,
)

fun toPrimaryNearMissAssignmentView(item: PrimaryNearMissAssignment): PrimaryNearMissAssignmentView {
    return PrimaryNearMissAssignmentView(
        code = item.code,
        labelText = truncate(item.label, 40),
        phoneText = item.phone,
        limitText = item.limit.toString(),
        carrierText = item.carrier,
        currency = item.currency,
        source = item.source,
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryNearMissAssignmentViews(items: MutableList<PrimaryNearMissAssignment>): MutableList<PrimaryNearMissAssignmentView> {
    val views = mutableListOf<PrimaryNearMissAssignmentView>()
    for (item in items) {
        views.add(toPrimaryNearMissAssignmentView(item))
    }
    return views
}

fun sumPrimaryNearMissAssignmentView(view: PrimaryNearMissAssignmentView): Double {
    val parsedLimit = view.limitText.toInt()
    return parsedLimit.toDouble()
}
