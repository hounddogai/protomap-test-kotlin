package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area cycle. */
data class ServiceAreaCycleView(
    var code: String,
    var name: String,
    var etaMinutesText: String,
    var dock: String,
    var ownerId: String,
    var trailerIdText: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toServiceAreaCycleView(item: ServiceAreaCycle): ServiceAreaCycleView {
    return ServiceAreaCycleView(
        code = item.code,
        name = truncate(item.name, 64),
        etaMinutesText = item.etaMinutes.toString(),
        dock = truncate(item.dock, 64),
        ownerId = item.ownerId,
        trailerIdText = item.trailerId,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaCycleViews(items: MutableList<ServiceAreaCycle>): MutableList<ServiceAreaCycleView> {
    val views = mutableListOf<ServiceAreaCycleView>()
    for (item in items) {
        views.add(toServiceAreaCycleView(item))
    }
    return views
}

fun sumServiceAreaCycleView(view: ServiceAreaCycleView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedEndsAt.toDouble()
}
