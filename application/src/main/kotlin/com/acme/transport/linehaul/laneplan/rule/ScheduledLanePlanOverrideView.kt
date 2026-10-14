package com.acme.transport.linehaul.laneplan.rule

/** How screens and exports show a scheduled lane plan override. */
data class ScheduledLanePlanOverrideView(
    var code: String,
    var name: String,
    var marginText: String,
    var unit: String,
    var weight: String,
    var statusLabel: String,
)

fun toScheduledLanePlanOverrideView(item: ScheduledLanePlanOverride): ScheduledLanePlanOverrideView {
    return ScheduledLanePlanOverrideView(
        code = item.code,
        name = item.name,
        marginText = "%.2f".format(item.margin),
        unit = item.unit,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLanePlanOverrideViews(items: MutableList<ScheduledLanePlanOverride>): MutableList<ScheduledLanePlanOverrideView> {
    val views = mutableListOf<ScheduledLanePlanOverrideView>()
    for (item in items) {
        views.add(toScheduledLanePlanOverrideView(item))
    }
    return views
}

fun sumScheduledLanePlanOverrideView(view: ScheduledLanePlanOverrideView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedMargin + parsedWeight
}
