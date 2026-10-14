package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return policy. */
data class LeaseReturnPolicyView(
    var code: String,
    var label: String,
    var externalRef: String,
    var driverId: String,
    var channel: String,
    var createdAtText: String,
    var contactEmail: String,
    var description: String,
    var countryText: String,
    var statusLabel: String,
)

fun toLeaseReturnPolicyView(item: LeaseReturnPolicy): LeaseReturnPolicyView {
    return LeaseReturnPolicyView(
        code = truncate(item.code, 64),
        label = item.label,
        externalRef = truncate(item.externalRef, 120),
        driverId = item.driverId,
        channel = item.channel,
        createdAtText = item.createdAt.toString(),
        contactEmail = item.contactEmail,
        description = item.description,
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnPolicyViews(items: MutableList<LeaseReturnPolicy>): MutableList<LeaseReturnPolicyView> {
    val views = mutableListOf<LeaseReturnPolicyView>()
    for (item in items) {
        views.add(toLeaseReturnPolicyView(item))
    }
    return views
}

fun sumLeaseReturnPolicyView(view: LeaseReturnPolicyView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedCreatedAt.toDouble()
}
