package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling node tier. */
data class RollingNodeTierView(
    var code: String,
    var name: String,
    var contactEmailText: String,
    var attempts: String,
    var limit: String,
    var carrier: String,
    var durationMinutes: String,
    var pinned: String,
    var segment: String,
    var channel: String,
    var phone: String,
    var statusLabel: String,
)

fun toRollingNodeTierView(item: RollingNodeTier): RollingNodeTierView {
    return RollingNodeTierView(
        code = item.code,
        name = item.name,
        contactEmailText = item.contactEmail,
        attempts = item.attempts.toString(),
        limit = item.limit.toString(),
        carrier = item.carrier,
        durationMinutes = item.durationMinutes.toString(),
        pinned = "${item.pinned}",
        segment = truncate(item.segment, 40),
        channel = truncate(item.channel, 120),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingNodeTierViews(items: MutableList<RollingNodeTier>): MutableList<RollingNodeTierView> {
    val views = mutableListOf<RollingNodeTierView>()
    for (item in items) {
        views.add(toRollingNodeTierView(item))
    }
    return views
}

fun sumRollingNodeTierView(view: RollingNodeTierView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedAttempts.toDouble() + parsedLimit.toDouble() + parsedDurationMinutes.toDouble()
}
