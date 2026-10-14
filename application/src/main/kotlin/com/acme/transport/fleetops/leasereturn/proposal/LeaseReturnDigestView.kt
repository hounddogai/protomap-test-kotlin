package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return digest. */
data class LeaseReturnDigestView(
    var code: String,
    var name: String,
    var priority: String,
    var threshold: String,
    var durationMinutes: String,
    var discount: String,
    var endsAt: String,
    var contactEmail: String,
    var locale: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toLeaseReturnDigestView(item: LeaseReturnDigest): LeaseReturnDigestView {
    return LeaseReturnDigestView(
        code = item.code,
        name = item.name,
        priority = item.priority.toString(),
        threshold = "%.2f".format(item.threshold),
        durationMinutes = item.durationMinutes.toString(),
        discount = "%.2f".format(item.discount),
        endsAt = item.endsAt.toString(),
        contactEmail = truncate(item.contactEmail, 80),
        locale = item.locale,
        routeCode = truncate(item.routeCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnDigestViews(items: MutableList<LeaseReturnDigest>): MutableList<LeaseReturnDigestView> {
    val views = mutableListOf<LeaseReturnDigestView>()
    for (item in items) {
        views.add(toLeaseReturnDigestView(item))
    }
    return views
}

fun sumLeaseReturnDigestView(view: LeaseReturnDigestView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedPriority.toDouble() + parsedThreshold + parsedDurationMinutes.toDouble()
}
