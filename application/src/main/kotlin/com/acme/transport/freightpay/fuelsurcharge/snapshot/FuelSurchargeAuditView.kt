package com.acme.transport.freightpay.fuelsurcharge.snapshot

/** How screens and exports show a fuel surcharge audit. */
data class FuelSurchargeAuditView(
    var code: String,
    var title: String,
    var unit: String,
    var updatedAt: String,
    var carrier: String,
    var statusLabel: String,
)

fun toFuelSurchargeAuditView(item: FuelSurchargeAudit): FuelSurchargeAuditView {
    return FuelSurchargeAuditView(
        code = item.code,
        title = item.title,
        unit = item.unit,
        updatedAt = item.updatedAt.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeAuditViews(items: MutableList<FuelSurchargeAudit>): MutableList<FuelSurchargeAuditView> {
    val views = mutableListOf<FuelSurchargeAuditView>()
    for (item in items) {
        views.add(toFuelSurchargeAuditView(item))
    }
    return views
}

fun sumFuelSurchargeAuditView(view: FuelSurchargeAuditView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedUpdatedAt.toDouble()
}
