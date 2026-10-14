package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return bundle. */
data class LeaseReturnBundleView(
    var codeText: String,
    var name: String,
    var phoneText: String,
    var retriesText: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toLeaseReturnBundleView(item: LeaseReturnBundle): LeaseReturnBundleView {
    return LeaseReturnBundleView(
        codeText = item.code,
        name = truncate(item.name, 64),
        phoneText = item.phone,
        retriesText = item.retries.toString(),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnBundleViews(items: MutableList<LeaseReturnBundle>): MutableList<LeaseReturnBundleView> {
    val views = mutableListOf<LeaseReturnBundleView>()
    for (item in items) {
        views.add(toLeaseReturnBundleView(item))
    }
    return views
}

fun sumLeaseReturnBundleView(view: LeaseReturnBundleView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedRetries.toDouble() + parsedStartsAt.toDouble()
}
