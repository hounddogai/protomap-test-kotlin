package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending near miss milestone. */
data class PendingNearMissMilestoneView(
    var code: String,
    var name: String,
    var country: String,
    var archived: String,
    var maximum: String,
    var statusLabel: String,
)

fun toPendingNearMissMilestoneView(item: PendingNearMissMilestone): PendingNearMissMilestoneView {
    return PendingNearMissMilestoneView(
        code = truncate(item.code, 40),
        name = item.name,
        country = item.country,
        archived = "${item.archived}",
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingNearMissMilestoneViews(items: MutableList<PendingNearMissMilestone>): MutableList<PendingNearMissMilestoneView> {
    val views = mutableListOf<PendingNearMissMilestoneView>()
    for (item in items) {
        views.add(toPendingNearMissMilestoneView(item))
    }
    return views
}

fun sumPendingNearMissMilestoneView(view: PendingNearMissMilestoneView): Double {
    val parsedMaximum = view.maximum.toDouble()
    return parsedMaximum
}
