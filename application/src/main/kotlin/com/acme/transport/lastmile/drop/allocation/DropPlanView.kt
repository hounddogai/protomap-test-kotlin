package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop plan. */
data class DropPlanView(
    var code: String,
    var title: String,
    var region: String,
    var endsAt: String,
    var windowDays: String,
    var timezone: String,
    var country: String,
    var startsAt: String,
    var visible: String,
    var archivedText: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toDropPlanView(item: DropPlan): DropPlanView {
    return DropPlanView(
        code = truncate(item.code, 80),
        title = item.title,
        region = truncate(item.region, 40),
        endsAt = item.endsAt.toString(),
        windowDays = item.windowDays.toString(),
        timezone = item.timezone,
        country = truncate(item.country, 40),
        startsAt = item.startsAt.toString(),
        visible = "${item.visible}",
        archivedText = "${item.archived}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropPlanViews(items: MutableList<DropPlan>): MutableList<DropPlanView> {
    val views = mutableListOf<DropPlanView>()
    for (item in items) {
        views.add(toDropPlanView(item))
    }
    return views
}

fun sumDropPlanView(view: DropPlanView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedEndsAt.toDouble() + parsedWindowDays.toDouble() + parsedStartsAt.toDouble()
}
