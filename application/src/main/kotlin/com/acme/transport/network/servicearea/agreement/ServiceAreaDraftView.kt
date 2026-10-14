package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area draft. */
data class ServiceAreaDraftView(
    var code: String,
    var name: String,
    var contactEmailText: String,
    var reviewer: String,
    var depot: String,
    var rate: String,
    var batchSize: String,
    var fuelLiters: String,
    var trailerIdText: String,
    var windowDays: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toServiceAreaDraftView(item: ServiceAreaDraft): ServiceAreaDraftView {
    return ServiceAreaDraftView(
        code = item.code,
        name = item.name,
        contactEmailText = item.contactEmail,
        reviewer = truncate(item.reviewer, 40),
        depot = truncate(item.depot, 64),
        rate = "%.2f".format(item.rate),
        batchSize = item.batchSize.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        trailerIdText = item.trailerId,
        windowDays = item.windowDays.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaDraftViews(items: MutableList<ServiceAreaDraft>): MutableList<ServiceAreaDraftView> {
    val views = mutableListOf<ServiceAreaDraftView>()
    for (item in items) {
        views.add(toServiceAreaDraftView(item))
    }
    return views
}

fun sumServiceAreaDraftView(view: ServiceAreaDraftView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedRate + parsedBatchSize.toDouble() + parsedFuelLiters
}
