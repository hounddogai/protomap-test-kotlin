package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area profile. */
data class ServiceAreaProfileView(
    var code: String,
    var name: String,
    var startsAt: String,
    var checksum: String,
    var depotText: String,
    var dockText: String,
    var country: String,
    var cost: String,
    var timezone: String,
    var statusLabel: String,
)

fun toServiceAreaProfileView(item: ServiceAreaProfile): ServiceAreaProfileView {
    return ServiceAreaProfileView(
        code = item.code,
        name = truncate(item.name, 80),
        startsAt = item.startsAt.toString(),
        checksum = item.checksum,
        depotText = item.depot,
        dockText = truncate(item.dock, 80),
        country = truncate(item.country, 120),
        cost = "%.2f".format(item.cost),
        timezone = truncate(item.timezone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaProfileViews(items: MutableList<ServiceAreaProfile>): MutableList<ServiceAreaProfileView> {
    val views = mutableListOf<ServiceAreaProfileView>()
    for (item in items) {
        views.add(toServiceAreaProfileView(item))
    }
    return views
}

fun sumServiceAreaProfileView(view: ServiceAreaProfileView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedStartsAt.toDouble() + parsedCost
}
