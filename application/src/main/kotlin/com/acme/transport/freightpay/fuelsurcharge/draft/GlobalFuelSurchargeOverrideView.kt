package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global fuel surcharge override. */
data class GlobalFuelSurchargeOverrideView(
    var codeText: String,
    var label: String,
    var discount: String,
    var routeCode: String,
    var ownerIdText: String,
    var name: String,
    var channel: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toGlobalFuelSurchargeOverrideView(item: GlobalFuelSurchargeOverride): GlobalFuelSurchargeOverrideView {
    return GlobalFuelSurchargeOverrideView(
        codeText = truncate(item.code, 64),
        label = item.label,
        discount = "%.2f".format(item.discount),
        routeCode = item.routeCode,
        ownerIdText = item.ownerId,
        name = item.name,
        channel = truncate(item.channel, 64),
        vehicleId = truncate(item.vehicleId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFuelSurchargeOverrideViews(items: MutableList<GlobalFuelSurchargeOverride>): MutableList<GlobalFuelSurchargeOverrideView> {
    val views = mutableListOf<GlobalFuelSurchargeOverrideView>()
    for (item in items) {
        views.add(toGlobalFuelSurchargeOverrideView(item))
    }
    return views
}

fun sumGlobalFuelSurchargeOverrideView(view: GlobalFuelSurchargeOverrideView): Double {
    val parsedDiscount = view.discount.toDouble()
    return parsedDiscount
}
