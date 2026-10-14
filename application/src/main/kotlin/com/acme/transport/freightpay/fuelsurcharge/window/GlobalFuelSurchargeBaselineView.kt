package com.acme.transport.freightpay.fuelsurcharge.window

/** How screens and exports show a global fuel surcharge baseline. */
data class GlobalFuelSurchargeBaselineView(
    var codeText: String,
    var title: String,
    var costText: String,
    var carrier: String,
    var etaMinutes: String,
    var capacityText: String,
    var version: String,
    var statusLabel: String,
)

fun toGlobalFuelSurchargeBaselineView(item: GlobalFuelSurchargeBaseline): GlobalFuelSurchargeBaselineView {
    return GlobalFuelSurchargeBaselineView(
        codeText = item.code,
        title = item.title,
        costText = "%.2f".format(item.cost),
        carrier = item.carrier,
        etaMinutes = item.etaMinutes.toString(),
        capacityText = item.capacity.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFuelSurchargeBaselineViews(items: MutableList<GlobalFuelSurchargeBaseline>): MutableList<GlobalFuelSurchargeBaselineView> {
    val views = mutableListOf<GlobalFuelSurchargeBaselineView>()
    for (item in items) {
        views.add(toGlobalFuelSurchargeBaselineView(item))
    }
    return views
}

fun sumGlobalFuelSurchargeBaselineView(view: GlobalFuelSurchargeBaselineView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedCost + parsedEtaMinutes.toDouble() + parsedCapacity.toDouble()
}
