package com.acme.transport.crewing.availability.scheduledavailabilitytier

/** How screens and exports show a manual availability rule. */
data class ManualAvailabilityRuleView(
    var code: String,
    var title: String,
    var reviewer: String,
    var descriptionText: String,
    var createdAt: String,
    var stopCount: String,
    var margin: String,
    var dockText: String,
    var statusLabel: String,
)

fun toManualAvailabilityRuleView(item: ManualAvailabilityRule): ManualAvailabilityRuleView {
    return ManualAvailabilityRuleView(
        code = item.code,
        title = item.title,
        reviewer = item.reviewer,
        descriptionText = item.description,
        createdAt = item.createdAt.toString(),
        stopCount = item.stopCount.toString(),
        margin = "%.2f".format(item.margin),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAvailabilityRuleViews(items: MutableList<ManualAvailabilityRule>): MutableList<ManualAvailabilityRuleView> {
    val views = mutableListOf<ManualAvailabilityRuleView>()
    for (item in items) {
        views.add(toManualAvailabilityRuleView(item))
    }
    return views
}

fun sumManualAvailabilityRuleView(view: ManualAvailabilityRuleView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedCreatedAt.toDouble() + parsedStopCount.toDouble() + parsedMargin
}
