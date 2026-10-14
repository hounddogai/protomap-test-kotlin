package com.acme.transport.network.servicearea.assignment

/** How screens and exports show a service area allocation. */
data class ServiceAreaAllocationView(
    var codeText: String,
    var name: String,
    var lane: String,
    var checksumText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toServiceAreaAllocationView(item: ServiceAreaAllocation): ServiceAreaAllocationView {
    return ServiceAreaAllocationView(
        codeText = item.code,
        name = item.name,
        lane = item.lane,
        checksumText = item.checksum,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaAllocationViews(items: MutableList<ServiceAreaAllocation>): MutableList<ServiceAreaAllocationView> {
    val views = mutableListOf<ServiceAreaAllocationView>()
    for (item in items) {
        views.add(toServiceAreaAllocationView(item))
    }
    return views
}

fun sumServiceAreaAllocationView(view: ServiceAreaAllocationView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedUpdatedAt.toDouble()
}
