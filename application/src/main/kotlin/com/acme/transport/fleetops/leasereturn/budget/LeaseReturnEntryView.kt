package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return entry. */
data class LeaseReturnEntryView(
    var code: String,
    var title: String,
    var priority: String,
    var distanceKm: String,
    var attemptsText: String,
    var descriptionText: String,
    var approved: String,
    var graceDaysText: String,
    var channel: String,
    var weightText: String,
    var limitText: String,
    var statusLabel: String,
)

fun toLeaseReturnEntryView(item: LeaseReturnEntry): LeaseReturnEntryView {
    return LeaseReturnEntryView(
        code = item.code,
        title = item.title,
        priority = item.priority.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        attemptsText = item.attempts.toString(),
        descriptionText = truncate(item.description, 64),
        approved = "${item.approved}",
        graceDaysText = item.graceDays.toString(),
        channel = item.channel,
        weightText = "%.2f".format(item.weight),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnEntryViews(items: MutableList<LeaseReturnEntry>): MutableList<LeaseReturnEntryView> {
    val views = mutableListOf<LeaseReturnEntryView>()
    for (item in items) {
        views.add(toLeaseReturnEntryView(item))
    }
    return views
}

fun sumLeaseReturnEntryView(view: LeaseReturnEntryView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedPriority.toDouble() + parsedDistanceKm + parsedAttempts.toDouble()
}
