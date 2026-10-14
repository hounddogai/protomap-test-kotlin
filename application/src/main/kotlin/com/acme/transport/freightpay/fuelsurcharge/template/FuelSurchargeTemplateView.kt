package com.acme.transport.freightpay.fuelsurcharge.template

/** How screens and exports show a fuel surcharge template. */
data class FuelSurchargeTemplateView(
    var code: String,
    var labelText: String,
    var capacityText: String,
    var expiresAt: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toFuelSurchargeTemplateView(item: FuelSurchargeTemplate): FuelSurchargeTemplateView {
    return FuelSurchargeTemplateView(
        code = item.code,
        labelText = item.label,
        capacityText = item.capacity.toString(),
        expiresAt = item.expiresAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeTemplateViews(items: MutableList<FuelSurchargeTemplate>): MutableList<FuelSurchargeTemplateView> {
    val views = mutableListOf<FuelSurchargeTemplateView>()
    for (item in items) {
        views.add(toFuelSurchargeTemplateView(item))
    }
    return views
}

fun sumFuelSurchargeTemplateView(view: FuelSurchargeTemplateView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCapacity.toDouble() + parsedExpiresAt.toDouble() + parsedLoadKg
}
