package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped service area mapping. */
data class CappedServiceAreaMappingView(
    var code: String,
    var label: String,
    var loadKg: String,
    var contactEmail: String,
    var description: String,
    var pallets: String,
    var statusLabel: String,
)

fun toCappedServiceAreaMappingView(item: CappedServiceAreaMapping): CappedServiceAreaMappingView {
    return CappedServiceAreaMappingView(
        code = item.code,
        label = item.label,
        loadKg = "%.2f".format(item.loadKg),
        contactEmail = truncate(item.contactEmail, 40),
        description = item.description,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedServiceAreaMappingViews(items: MutableList<CappedServiceAreaMapping>): MutableList<CappedServiceAreaMappingView> {
    val views = mutableListOf<CappedServiceAreaMappingView>()
    for (item in items) {
        views.add(toCappedServiceAreaMappingView(item))
    }
    return views
}

fun sumCappedServiceAreaMappingView(view: CappedServiceAreaMappingView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedLoadKg + parsedPallets.toDouble()
}
