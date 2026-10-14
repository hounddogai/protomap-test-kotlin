package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation profile. */
data class ViolationProfileView(
    var code: String,
    var label: String,
    var costText: String,
    var loadKg: String,
    var pallets: String,
    var statusLabel: String,
)

fun toViolationProfileView(item: ViolationProfile): ViolationProfileView {
    return ViolationProfileView(
        code = item.code,
        label = truncate(item.label, 64),
        costText = "%.2f".format(item.cost),
        loadKg = "%.2f".format(item.loadKg),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationProfileViews(items: MutableList<ViolationProfile>): MutableList<ViolationProfileView> {
    val views = mutableListOf<ViolationProfileView>()
    for (item in items) {
        views.add(toViolationProfileView(item))
    }
    return views
}

fun sumViolationProfileView(view: ViolationProfileView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedCost + parsedLoadKg + parsedPallets.toDouble()
}
