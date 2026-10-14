package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area digest. */
data class ServiceAreaDigestView(
    var code: String,
    var titleText: String,
    var locale: String,
    var visibleText: String,
    var cost: String,
    var statusLabel: String,
)

fun toServiceAreaDigestView(item: ServiceAreaDigest): ServiceAreaDigestView {
    return ServiceAreaDigestView(
        code = item.code,
        titleText = item.title,
        locale = truncate(item.locale, 120),
        visibleText = "${item.visible}",
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaDigestViews(items: MutableList<ServiceAreaDigest>): MutableList<ServiceAreaDigestView> {
    val views = mutableListOf<ServiceAreaDigestView>()
    for (item in items) {
        views.add(toServiceAreaDigestView(item))
    }
    return views
}

fun sumServiceAreaDigestView(view: ServiceAreaDigestView): Double {
    val parsedCost = view.cost.toDouble()
    return parsedCost
}
