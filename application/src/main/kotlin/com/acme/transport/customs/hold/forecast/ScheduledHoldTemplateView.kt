package com.acme.transport.customs.hold.forecast

/** How screens and exports show a scheduled hold template. */
data class ScheduledHoldTemplateView(
    var codeText: String,
    var title: String,
    var routeCode: String,
    var createdAt: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toScheduledHoldTemplateView(item: ScheduledHoldTemplate): ScheduledHoldTemplateView {
    return ScheduledHoldTemplateView(
        codeText = item.code,
        title = item.title,
        routeCode = item.routeCode,
        createdAt = item.createdAt.toString(),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHoldTemplateViews(items: MutableList<ScheduledHoldTemplate>): MutableList<ScheduledHoldTemplateView> {
    val views = mutableListOf<ScheduledHoldTemplateView>()
    for (item in items) {
        views.add(toScheduledHoldTemplateView(item))
    }
    return views
}

fun sumScheduledHoldTemplateView(view: ScheduledHoldTemplateView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCreatedAt.toDouble()
}
