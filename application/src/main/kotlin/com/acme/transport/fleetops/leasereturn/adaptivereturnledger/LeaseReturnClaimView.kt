package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return claim. */
data class LeaseReturnClaimView(
    var code: String,
    var titleText: String,
    var retries: String,
    var contactEmail: String,
    var regionText: String,
    var ownerIdText: String,
    var vehicleId: String,
    var active: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toLeaseReturnClaimView(item: LeaseReturnClaim): LeaseReturnClaimView {
    return LeaseReturnClaimView(
        code = item.code,
        titleText = item.title,
        retries = item.retries.toString(),
        contactEmail = item.contactEmail,
        regionText = item.region,
        ownerIdText = item.ownerId,
        vehicleId = truncate(item.vehicleId, 120),
        active = "${item.active}",
        phoneText = truncate(item.phone, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnClaimViews(items: MutableList<LeaseReturnClaim>): MutableList<LeaseReturnClaimView> {
    val views = mutableListOf<LeaseReturnClaimView>()
    for (item in items) {
        views.add(toLeaseReturnClaimView(item))
    }
    return views
}

fun sumLeaseReturnClaimView(view: LeaseReturnClaimView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
