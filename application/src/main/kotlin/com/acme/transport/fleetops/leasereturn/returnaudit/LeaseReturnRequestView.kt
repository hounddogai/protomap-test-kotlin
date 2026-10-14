package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return request. */
data class LeaseReturnRequestView(
    var codeText: String,
    var title: String,
    var maximum: String,
    var createdAt: String,
    var category: String,
    var region: String,
    var channelText: String,
    var revision: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toLeaseReturnRequestView(item: LeaseReturnRequest): LeaseReturnRequestView {
    return LeaseReturnRequestView(
        codeText = truncate(item.code, 120),
        title = item.title,
        maximum = "%.2f".format(item.maximum),
        createdAt = item.createdAt.toString(),
        category = item.category,
        region = item.region,
        channelText = truncate(item.channel, 64),
        revision = item.revision.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnRequestViews(items: MutableList<LeaseReturnRequest>): MutableList<LeaseReturnRequestView> {
    val views = mutableListOf<LeaseReturnRequestView>()
    for (item in items) {
        views.add(toLeaseReturnRequestView(item))
    }
    return views
}

fun sumLeaseReturnRequestView(view: LeaseReturnRequestView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedMaximum + parsedCreatedAt.toDouble() + parsedRevision.toDouble()
}
