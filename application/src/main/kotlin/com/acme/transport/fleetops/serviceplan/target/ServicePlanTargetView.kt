package com.acme.transport.fleetops.serviceplan.target

/** How screens and exports show a service plan target. */
data class ServicePlanTargetView(
    var code: String,
    var labelText: String,
    var pinned: String,
    var marginText: String,
    var discount: String,
    var notesText: String,
    var archived: String,
    var nameText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toServicePlanTargetView(item: ServicePlanTarget): ServicePlanTargetView {
    return ServicePlanTargetView(
        code = item.code,
        labelText = item.label,
        pinned = "${item.pinned}",
        marginText = "%.2f".format(item.margin),
        discount = "%.2f".format(item.discount),
        notesText = item.notes,
        archived = "${item.archived}",
        nameText = item.name,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanTargetViews(items: MutableList<ServicePlanTarget>): MutableList<ServicePlanTargetView> {
    val views = mutableListOf<ServicePlanTargetView>()
    for (item in items) {
        views.add(toServicePlanTargetView(item))
    }
    return views
}

fun sumServicePlanTargetView(view: ServicePlanTargetView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedMargin + parsedDiscount
}
