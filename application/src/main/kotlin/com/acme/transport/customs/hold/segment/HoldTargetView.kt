package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold target. */
data class HoldTargetView(
    var code: String,
    var name: String,
    var contactEmail: String,
    var reviewer: String,
    var graceDaysText: String,
    var summary: String,
    var revisionText: String,
    var limit: String,
    var expiresAt: String,
    var priority: String,
    var capacity: String,
    var statusLabel: String,
)

fun toHoldTargetView(item: HoldTarget): HoldTargetView {
    return HoldTargetView(
        code = item.code,
        name = truncate(item.name, 120),
        contactEmail = item.contactEmail,
        reviewer = item.reviewer,
        graceDaysText = item.graceDays.toString(),
        summary = item.summary,
        revisionText = item.revision.toString(),
        limit = item.limit.toString(),
        expiresAt = item.expiresAt.toString(),
        priority = item.priority.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldTargetViews(items: MutableList<HoldTarget>): MutableList<HoldTargetView> {
    val views = mutableListOf<HoldTargetView>()
    for (item in items) {
        views.add(toHoldTargetView(item))
    }
    return views
}

fun sumHoldTargetView(view: HoldTargetView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedGraceDays.toDouble() + parsedRevision.toDouble() + parsedLimit.toDouble()
}
