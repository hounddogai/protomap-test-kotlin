package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending service area entry. */
data class PendingServiceAreaEntryView(
    var codeText: String,
    var title: String,
    var contactEmail: String,
    var margin: String,
    var archived: String,
    var fuelLiters: String,
    var description: String,
    var budget: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toPendingServiceAreaEntryView(item: PendingServiceAreaEntry): PendingServiceAreaEntryView {
    return PendingServiceAreaEntryView(
        codeText = item.code,
        title = truncate(item.title, 40),
        contactEmail = item.contactEmail,
        margin = "%.2f".format(item.margin),
        archived = "${item.archived}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        description = item.description,
        budget = "%.2f".format(item.budget),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingServiceAreaEntryViews(items: MutableList<PendingServiceAreaEntry>): MutableList<PendingServiceAreaEntryView> {
    val views = mutableListOf<PendingServiceAreaEntryView>()
    for (item in items) {
        views.add(toPendingServiceAreaEntryView(item))
    }
    return views
}

fun sumPendingServiceAreaEntryView(view: PendingServiceAreaEntryView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedMargin + parsedFuelLiters + parsedBudget
}
