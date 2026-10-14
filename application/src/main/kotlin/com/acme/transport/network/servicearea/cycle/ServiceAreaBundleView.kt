package com.acme.transport.network.servicearea.cycle

/** How screens and exports show a service area bundle. */
data class ServiceAreaBundleView(
    var code: String,
    var titleText: String,
    var fuelLiters: String,
    var capacity: String,
    var externalRef: String,
    var segment: String,
    var regionText: String,
    var pinnedText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toServiceAreaBundleView(item: ServiceAreaBundle): ServiceAreaBundleView {
    return ServiceAreaBundleView(
        code = item.code,
        titleText = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        capacity = item.capacity.toString(),
        externalRef = item.externalRef,
        segment = item.segment,
        regionText = item.region,
        pinnedText = "${item.pinned}",
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaBundleViews(items: MutableList<ServiceAreaBundle>): MutableList<ServiceAreaBundleView> {
    val views = mutableListOf<ServiceAreaBundleView>()
    for (item in items) {
        views.add(toServiceAreaBundleView(item))
    }
    return views
}

fun sumServiceAreaBundleView(view: ServiceAreaBundleView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedFuelLiters + parsedCapacity.toDouble() + parsedRatio
}
