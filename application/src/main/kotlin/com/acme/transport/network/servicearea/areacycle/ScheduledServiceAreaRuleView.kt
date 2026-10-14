package com.acme.transport.network.servicearea.areacycle

/** How screens and exports show a scheduled service area rule. */
data class ScheduledServiceAreaRuleView(
    var code: String,
    var name: String,
    var reviewer: String,
    var notesText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toScheduledServiceAreaRuleView(item: ScheduledServiceAreaRule): ScheduledServiceAreaRuleView {
    return ScheduledServiceAreaRuleView(
        code = item.code,
        name = item.name,
        reviewer = item.reviewer,
        notesText = item.notes,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledServiceAreaRuleViews(items: MutableList<ScheduledServiceAreaRule>): MutableList<ScheduledServiceAreaRuleView> {
    val views = mutableListOf<ScheduledServiceAreaRuleView>()
    for (item in items) {
        views.add(toScheduledServiceAreaRuleView(item))
    }
    return views
}

fun sumScheduledServiceAreaRuleView(view: ScheduledServiceAreaRuleView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}
