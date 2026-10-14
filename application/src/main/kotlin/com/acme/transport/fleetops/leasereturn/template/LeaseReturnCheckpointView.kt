package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return checkpoint. */
data class LeaseReturnCheckpointView(
    var code: String,
    var title: String,
    var maximum: String,
    var threshold: String,
    var attempts: String,
    var countryText: String,
    var discountText: String,
    var carrier: String,
    var quantity: String,
    var priority: String,
    var statusLabel: String,
)

fun toLeaseReturnCheckpointView(item: LeaseReturnCheckpoint): LeaseReturnCheckpointView {
    return LeaseReturnCheckpointView(
        code = item.code,
        title = item.title,
        maximum = "%.2f".format(item.maximum),
        threshold = "%.2f".format(item.threshold),
        attempts = item.attempts.toString(),
        countryText = truncate(item.country, 80),
        discountText = "%.2f".format(item.discount),
        carrier = item.carrier,
        quantity = item.quantity.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnCheckpointViews(items: MutableList<LeaseReturnCheckpoint>): MutableList<LeaseReturnCheckpointView> {
    val views = mutableListOf<LeaseReturnCheckpointView>()
    for (item in items) {
        views.add(toLeaseReturnCheckpointView(item))
    }
    return views
}

fun sumLeaseReturnCheckpointView(view: LeaseReturnCheckpointView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedMaximum + parsedThreshold + parsedAttempts.toDouble()
}
