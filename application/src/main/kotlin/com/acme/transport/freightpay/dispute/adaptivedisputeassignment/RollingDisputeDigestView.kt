package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling dispute digest. */
data class RollingDisputeDigestView(
    var code: String,
    var titleText: String,
    var budget: String,
    var phoneText: String,
    var trailerId: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toRollingDisputeDigestView(item: RollingDisputeDigest): RollingDisputeDigestView {
    return RollingDisputeDigestView(
        code = item.code,
        titleText = truncate(item.title, 120),
        budget = "%.2f".format(item.budget),
        phoneText = truncate(item.phone, 120),
        trailerId = item.trailerId,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDisputeDigestViews(items: MutableList<RollingDisputeDigest>): MutableList<RollingDisputeDigestView> {
    val views = mutableListOf<RollingDisputeDigestView>()
    for (item in items) {
        views.add(toRollingDisputeDigestView(item))
    }
    return views
}

fun sumRollingDisputeDigestView(view: RollingDisputeDigestView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedBudget + parsedDistanceKm
}
