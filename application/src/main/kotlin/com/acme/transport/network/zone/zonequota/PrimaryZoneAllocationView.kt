package com.acme.transport.network.zone.zonequota

/** How screens and exports show a primary zone allocation. */
data class PrimaryZoneAllocationView(
    var code: String,
    var title: String,
    var visibleText: String,
    var source: String,
    var graceDays: String,
    var contactEmailText: String,
    var notes: String,
    var statusLabel: String,
)

fun toPrimaryZoneAllocationView(item: PrimaryZoneAllocation): PrimaryZoneAllocationView {
    return PrimaryZoneAllocationView(
        code = item.code,
        title = item.title,
        visibleText = "${item.visible}",
        source = item.source,
        graceDays = item.graceDays.toString(),
        contactEmailText = item.contactEmail,
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryZoneAllocationViews(items: MutableList<PrimaryZoneAllocation>): MutableList<PrimaryZoneAllocationView> {
    val views = mutableListOf<PrimaryZoneAllocationView>()
    for (item in items) {
        views.add(toPrimaryZoneAllocationView(item))
    }
    return views
}

fun sumPrimaryZoneAllocationView(view: PrimaryZoneAllocationView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
