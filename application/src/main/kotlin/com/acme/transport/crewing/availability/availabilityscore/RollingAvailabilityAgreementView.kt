package com.acme.transport.crewing.availability.availabilityscore

/** How screens and exports show a rolling availability agreement. */
data class RollingAvailabilityAgreementView(
    var codeText: String,
    var title: String,
    var trailerId: String,
    var limit: String,
    var cost: String,
    var fuelLitersText: String,
    var priorityText: String,
    var notesText: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toRollingAvailabilityAgreementView(item: RollingAvailabilityAgreement): RollingAvailabilityAgreementView {
    return RollingAvailabilityAgreementView(
        codeText = item.code,
        title = item.title,
        trailerId = item.trailerId,
        limit = item.limit.toString(),
        cost = "%.2f".format(item.cost),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        priorityText = item.priority.toString(),
        notesText = item.notes,
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAvailabilityAgreementViews(items: MutableList<RollingAvailabilityAgreement>): MutableList<RollingAvailabilityAgreementView> {
    val views = mutableListOf<RollingAvailabilityAgreementView>()
    for (item in items) {
        views.add(toRollingAvailabilityAgreementView(item))
    }
    return views
}

fun sumRollingAvailabilityAgreementView(view: RollingAvailabilityAgreementView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedLimit.toDouble() + parsedCost + parsedFuelLiters
}
